package com.aspire.asat.notification.service;

import com.aspire.asat.common.dto.notification.NotificationEventRequestDto;
import com.aspire.asat.common.dto.notification.NotificationRecipientBundleDto;
import com.aspire.asat.common.dto.notification.NotificationRequestDto;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.client.RegistrationServiceClient;
import com.aspire.asat.notification.dto.dispatch.NotificationDispatchResultDto;
import com.aspire.asat.notification.enums.DeliveryStatus;
import com.aspire.asat.notification.service.support.NotificationRecipientResolver;
import com.aspire.asat.notification.service.support.NotificationRecipientTarget;
import com.aspire.asat.notification.service.support.NotificationTemplateModelEnricher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Owns fan-out of a single notification event to every applicable recipient role
 * (User, Client Admin, MSP, Aspire Admin). Resolves the recipient hierarchy from the
 * registration service, applies the global -&gt; role -&gt; org -&gt; user gate chain per
 * recipient, and isolates per-recipient failures so one bad address cannot abort the others.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatchService {

    private static final List<NotificationRecipientRole> ALL_ROLES = List.of(
            NotificationRecipientRole.USER,
            NotificationRecipientRole.CLIENT_ADMIN,
            NotificationRecipientRole.MSP,
            NotificationRecipientRole.ASPIRE_ADMIN);

    private final NotificationSettingsService globalSettingsService;
    private final NotificationPreferenceResolver preferenceResolver;
    private final RegistrationServiceClient registrationServiceClient;
    private final NotificationRecipientResolver recipientResolver;
    private final NotificationTemplateModelEnricher templateModelEnricher;
    private final NotificationDeliveryService notificationDeliveryService;
    private final NotificationHistoryService notificationHistoryService;

    public NotificationDispatchResultDto dispatch(NotificationEventRequestDto event) {
        NotificationType type = event.getNotificationType();
        List<NotificationRecipientRole> requestedRoles = (event.getRecipientRoles() != null && !event.getRecipientRoles().isEmpty())
                ? event.getRecipientRoles() : ALL_ROLES;

        if (!globalSettingsService.isNotificationTypeEnabled(type)) {
            log.warn("Notification type {} is disabled globally; skipping role-based dispatch for user {}",
                    type, event.getTargetUserId());
            return emptyResult(type, event.getTargetUserId(), requestedRoles);
        }

        Optional<NotificationRecipientBundleDto> bundleOpt =
                registrationServiceClient.getNotificationRecipientBundle(event.getTargetUserId());
        if (bundleOpt.isEmpty()) {
            log.warn("Could not resolve recipient bundle for userId {}; skipping dispatch for type {}",
                    event.getTargetUserId(), type);
            return emptyResult(type, event.getTargetUserId(), requestedRoles);
        }

        NotificationRecipientBundleDto bundle = bundleOpt.get();
        int sentCount = 0;
        List<NotificationRecipientRole> skippedRoles = new ArrayList<>();

        for (NotificationRecipientRole role : requestedRoles) {
            List<NotificationRecipientTarget> targets = recipientResolver.resolveTargets(role, bundle);
            if (targets.isEmpty()) {
                log.debug("No recipient resolved for role {} on type {}, skipping", role, type);
                skippedRoles.add(role);
                continue;
            }

            boolean anySentForRole = false;
            for (NotificationRecipientTarget target : targets) {
                if (!preferenceResolver.isTypeAllowed(type, role, target.clientAdminId(), target.userId())) {
                    log.info("Notification type {} gated off for role {} (recipient {}), skipping", type, role, target.userId());
                    continue;
                }
                try {
                    dispatchToRecipient(event, role, target, bundle);
                    sentCount++;
                    anySentForRole = true;
                } catch (Exception e) {
                    log.error("Failed to dispatch notification type {} to role {} recipient {}: {}",
                            type, role, target.userId(), e.getMessage(), e);
                }
            }
            if (!anySentForRole) {
                skippedRoles.add(role);
            }
        }

        return NotificationDispatchResultDto.builder()
                .notificationType(type)
                .targetUserId(event.getTargetUserId())
                .sentCount(sentCount)
                .skippedRoles(skippedRoles)
                .build();
    }

    private void dispatchToRecipient(NotificationEventRequestDto event, NotificationRecipientRole role,
                                      NotificationRecipientTarget target, NotificationRecipientBundleDto bundle) {
        NotificationRequestDto request = NotificationRequestDto.builder()
                .to(target.email())
                .userId(target.userId())
                .clientAdminId(target.clientAdminId())
                .phoneNumber(target.phoneNumber())
                .recipientRole(role)
                .notificationType(event.getNotificationType())
                .channels(event.getChannels())
                .templateModel(templateModelEnricher.merge(
                        event.getTemplateModel(), event.getTemplateModelByRole(), role, bundle, target))
                .attachments(event.getAttachments())
                .metadata(event.getMetadata())
                .priority(event.getPriority())
                .build();

        String logId = UUID.randomUUID().toString();
        notificationHistoryService.createNotificationLog(request, logId);
        try {
            notificationDeliveryService.deliverNotification(request, logId);
            notificationHistoryService.finalizeNotificationLog(logId, DeliveryStatus.SUCCESS);
        } catch (Exception e) {
            notificationHistoryService.finalizeNotificationLog(logId, DeliveryStatus.FAILED);
            throw e;
        }
    }

    private NotificationDispatchResultDto emptyResult(NotificationType type, String targetUserId, List<NotificationRecipientRole> skippedRoles) {
        return NotificationDispatchResultDto.builder()
                .notificationType(type)
                .targetUserId(targetUserId)
                .sentCount(0)
                .skippedRoles(skippedRoles)
                .build();
    }
}
