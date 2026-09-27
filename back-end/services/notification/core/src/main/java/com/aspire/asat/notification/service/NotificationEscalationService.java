package com.aspire.asat.notification.service;

import com.aspire.asat.common.dto.notification.NotificationRecipientBundleDto;
import com.aspire.asat.common.dto.notification.NotificationRequestDto;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.client.RegistrationServiceClient;
import com.aspire.asat.notification.enums.DeliveryStatus;
import com.aspire.asat.notification.service.support.NotificationRecipientResolver;
import com.aspire.asat.notification.service.support.NotificationRecipientTarget;
import com.aspire.asat.notification.service.support.NotificationTemplateModelEnricher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Legacy bridge that lets MSP and Aspire Admin start receiving notifications without any
 * caller-side changes. After a legacy {@code POST /api/v1/send-notification} request is
 * delivered to its original single recipient, this service independently resolves the
 * recipient hierarchy for the same event and fans out to {@code MSP} and
 * {@code ASPIRE_ADMIN} whenever the role settings matrix allows it.
 *
 * <p>Runs after the primary delivery and never lets an escalation failure affect it: every
 * public entry point swallows and logs exceptions rather than propagating them.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEscalationService {

    private static final String DEDUP_KEY_PREFIX = "asat:notification:escalation:";
    private static final long DEDUP_TTL_MS = Duration.ofMinutes(10).toMillis();
    private static final int DEDUP_MAP_PRUNE_THRESHOLD = 10_000;

    private static final List<NotificationRecipientRole> ESCALATION_ROLES =
            List.of(NotificationRecipientRole.MSP, NotificationRecipientRole.ASPIRE_ADMIN);

    private final RegistrationServiceClient registrationServiceClient;
    private final NotificationRecipientResolver recipientResolver;
    private final NotificationTemplateModelEnricher templateModelEnricher;
    private final NotificationPreferenceResolver preferenceResolver;
    private final NotificationDeliveryService notificationDeliveryService;
    private final NotificationHistoryService notificationHistoryService;
    private final ObjectMapper objectMapper;

    /** In-process dedup of escalations for the same logical event (e.g. user + admin dual send). */
    private final ConcurrentHashMap<String, Long> recentEscalations = new ConcurrentHashMap<>();

    /**
     * Attempt to escalate a legacy notification request to MSP and Aspire Admin. Safe to call
     * unconditionally after every primary delivery -- all failure modes are logged and swallowed.
     *
     * @param originalRequest the request that was just delivered to its primary recipient
     * @param parentLogId     the {@code NotificationHistory} id of the primary delivery, recorded
     *                        on escalated rows so the admin panel can group the fan-out together
     */
    @Async
    public void escalate(NotificationRequestDto originalRequest, String parentLogId) {
        try {
            if (originalRequest.getRecipientRole() != null) {
                log.debug("Skipping escalation for type {}: request already carries recipientRole {}",
                        originalRequest.getNotificationType(), originalRequest.getRecipientRole());
                return;
            }

            String anchorUserId = firstNonBlank(originalRequest.getUserId(), originalRequest.getClientAdminId());
            if (isBlank(anchorUserId)) {
                log.debug("Skipping escalation for type {}: no userId or clientAdminId to anchor recipient resolution",
                        originalRequest.getNotificationType());
                return;
            }

            NotificationType type = originalRequest.getNotificationType();
            Optional<NotificationRecipientBundleDto> bundleOpt =
                    registrationServiceClient.getNotificationRecipientBundle(anchorUserId);
            if (bundleOpt.isEmpty()) {
                log.debug("Skipping escalation for type {}: could not resolve recipient bundle for {}", type, anchorUserId);
                return;
            }

            NotificationRecipientBundleDto bundle = bundleOpt.get();
            String eventHash = computeEventHash(originalRequest);

            for (NotificationRecipientRole role : ESCALATION_ROLES) {
                escalateToRole(originalRequest, parentLogId, type, role, bundle, eventHash);
            }
        } catch (Exception e) {
            log.error("Notification escalation failed for type {}: {}", originalRequest.getNotificationType(), e.getMessage(), e);
        }
    }

    private void escalateToRole(NotificationRequestDto originalRequest, String parentLogId, NotificationType type,
                                 NotificationRecipientRole role, NotificationRecipientBundleDto bundle, String eventHash) {
        List<NotificationRecipientTarget> targets = recipientResolver.resolveTargets(role, bundle);
        if (targets.isEmpty()) {
            return;
        }

        for (NotificationRecipientTarget target : targets) {
            try {
                if (!preferenceResolver.isTypeAllowed(type, role, target.clientAdminId(), target.userId())) {
                    log.debug("Escalation skipped: type {} disabled for role {} (recipient {})", type, role, target.userId());
                    continue;
                }

                String recipientId = firstNonBlank(target.userId(), target.email());
                String dedupKey = DEDUP_KEY_PREFIX + type + ":" + role + ":" + recipientId + ":" + eventHash;
                if (!acquireDedupLock(dedupKey)) {
                    log.debug("Escalation skipped: duplicate event for key {}", dedupKey);
                    continue;
                }

                deliverEscalation(originalRequest, parentLogId, role, target, bundle);
            } catch (Exception e) {
                log.error("Failed to escalate notification type {} to role {} recipient {}: {}",
                        type, role, target.userId(), e.getMessage(), e);
            }
        }
    }

    private void deliverEscalation(NotificationRequestDto originalRequest, String parentLogId,
                                    NotificationRecipientRole role, NotificationRecipientTarget target,
                                    NotificationRecipientBundleDto bundle) {
        Map<String, Object> metadata = new HashMap<>();
        if (originalRequest.getMetadata() != null) {
            metadata.putAll(originalRequest.getMetadata());
        }
        if (parentLogId != null) {
            metadata.put("parentLogId", parentLogId);
        }
        metadata.put("escalated", true);

        NotificationRequestDto escalationRequest = NotificationRequestDto.builder()
                .to(target.email())
                .userId(target.userId())
                .clientAdminId(target.clientAdminId())
                .phoneNumber(target.phoneNumber())
                .recipientRole(role)
                .notificationType(originalRequest.getNotificationType())
                .channels(originalRequest.getChannels())
                .templateModel(templateModelEnricher.enrich(
                        originalRequest.getTemplateModel(), role, bundle, target))
                .attachments(originalRequest.getAttachments())
                .metadata(metadata)
                .priority(originalRequest.getPriority())
                .build();

        String logId = UUID.randomUUID().toString();
        notificationHistoryService.createNotificationLog(escalationRequest, logId);
        try {
            notificationDeliveryService.deliverNotification(escalationRequest, logId);
            notificationHistoryService.finalizeNotificationLog(logId, DeliveryStatus.SUCCESS);
        } catch (Exception e) {
            notificationHistoryService.finalizeNotificationLog(logId, DeliveryStatus.FAILED);
            throw e;
        }
    }

    /**
     * True the first time this key is seen within the TTL window on this JVM instance.
     * Collapses duplicate legacy sends for the same logical event (e.g. user + admin dual mail).
     */
    private boolean acquireDedupLock(String dedupKey) {
        long now = System.currentTimeMillis();
        if (recentEscalations.size() > DEDUP_MAP_PRUNE_THRESHOLD) {
            recentEscalations.entrySet().removeIf(entry -> entry.getValue() < now);
        }

        AtomicBoolean claimed = new AtomicBoolean(false);
        recentEscalations.compute(dedupKey, (key, existingExpiry) -> {
            if (existingExpiry == null || existingExpiry < now) {
                claimed.set(true);
                return now + DEDUP_TTL_MS;
            }
            return existingExpiry;
        });
        return claimed.get();
    }

    /**
     * Derive a stable hash for the event: the caller-supplied {@code metadata.eventKey} when
     * present, otherwise a hash of the (order-independent) template model. This is what lets
     * two legacy calls for the same logical event (e.g. one type mailed to both the user and
     * the client admin) collapse into a single MSP/Aspire Admin notification.
     */
    private String computeEventHash(NotificationRequestDto request) {
        Object eventKey = request.getMetadata() != null ? request.getMetadata().get("eventKey") : null;
        if (eventKey != null && !eventKey.toString().isBlank()) {
            return sha256Hex(eventKey.toString());
        }
        try {
            Map<String, Object> sortedModel = new TreeMap<>();
            if (request.getTemplateModel() != null) {
                sortedModel.putAll(request.getTemplateModel());
            }
            return sha256Hex(objectMapper.writeValueAsString(sortedModel));
        } catch (Exception e) {
            log.warn("Failed to hash template model for dedup, falling back to notification type only: {}", e.getMessage());
            return sha256Hex(String.valueOf(request.getNotificationType()));
        }
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.trim().isEmpty()) {
            return a;
        }
        return b;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
