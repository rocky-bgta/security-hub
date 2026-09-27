package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.client.InsecureWebClient;
import com.aspire.asat.phishing.dto.enums.BreachSeverity;
import com.aspire.asat.phishing.dto.enums.BreachStatus;
import com.aspire.asat.phishing.dto.enums.RecipientBreachStatus;
import com.aspire.asat.phishing.dto.request.BreachConfigRequest;
import com.aspire.asat.phishing.dto.request.BreachStatusRequest;
import com.aspire.asat.phishing.dto.request.RecipientActionRequest;
import com.aspire.asat.phishing.dto.response.*;
import com.aspire.asat.phishing.model.ActionLog;
import com.aspire.asat.phishing.model.BreachDetectionConfig;
import com.aspire.asat.phishing.model.BreachRecord;
import com.aspire.asat.phishing.model.RecipientBreach;
import com.aspire.asat.phishing.repository.BreachDetectionConfigRepository;
import com.aspire.asat.phishing.repository.BreachRecordRepository;
import com.aspire.asat.phishing.repository.RecipientBreachRepository;
import com.aspire.asat.phishing.service.BreachService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for breach operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BreachServiceImpl implements BreachService {

    private final UserCurrentContextService userCurrentContextService;
    private final BreachRecordRepository breachRecordRepository;
    private final RecipientBreachRepository recipientBreachRepository;
    private final BreachDetectionConfigRepository configRepository;
    private final InsecureWebClient insecureWebClient;

    // ==================== Breach Record Operations ====================

    @Override
    public List<BreachRecordDto> getBreaches(int offset, int pageSize, String keyword,
            String domain, BreachStatus status, BreachSeverity severity,
            Instant startDate, Instant endDate) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        PageRequest pageable = PageRequest.of(offset / pageSize, pageSize);

        Page<BreachRecord> breaches;

        if (keyword != null && !keyword.isEmpty()) {
            breaches = breachRecordRepository.searchBreaches(clientId, keyword, pageable);
        } else if (domain != null && !domain.isEmpty()) {
            breaches = breachRecordRepository.findByClientIdAndDomainOrderByDateOfBreachDesc(
                    clientId, domain, pageable);
        } else if (status != null) {
            breaches = breachRecordRepository.findByClientIdAndStatusOrderByCreatedAtDesc(
                    clientId, status, pageable);
        } else if (severity != null) {
            breaches = breachRecordRepository.findByClientIdAndSeverityOrderByCreatedAtDesc(
                    clientId, severity, pageable);
        } else if (startDate != null && endDate != null) {
            breaches = breachRecordRepository.findByClientIdAndDateRange(
                    clientId, startDate, endDate, pageable);
        } else {
            breaches = breachRecordRepository.findByClientIdOrderByCreatedAtDesc(clientId, pageable);
        }

        return breaches.getContent().stream()
                .map(this::toBreachRecordDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countBreaches(String keyword, String domain, BreachStatus status,
            BreachSeverity severity, Instant startDate, Instant endDate) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();

        if (status != null) {
            return breachRecordRepository.countByClientIdAndStatus(clientId, status);
        } else if (severity != null) {
            return breachRecordRepository.countByClientIdAndSeverity(clientId, severity);
        }
        return breachRecordRepository.countByClientId(clientId);
    }

    @Override
    public BreachRecordDto getBreachById(String id) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        BreachRecord breach = breachRecordRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new RuntimeException("Breach record not found"));
        return toBreachRecordDto(breach);
    }

    @Override
    @Transactional
    public BreachRecordDto updateBreachStatus(String id, BreachStatusRequest request) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        BreachRecord breach = breachRecordRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new RuntimeException("Breach record not found"));

        breach.setStatus(request.getStatus());
        breach = breachRecordRepository.save(breach);

        log.info("Updated breach {} status to {}", id, request.getStatus());
        return toBreachRecordDto(breach);
    }

    @Override
    @Transactional
    public void deleteBreach(String id) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        BreachRecord breach = breachRecordRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new RuntimeException("Breach record not found"));

        // Delete associated recipient breaches
        recipientBreachRepository.deleteByBreachRecordId(id);
        breachRecordRepository.delete(breach);

        log.info("Deleted breach record {}", id);
    }

    @Override
    public byte[] exportBreaches(String format, String domain, BreachStatus status) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();

        List<BreachRecord> breaches;
        if (status != null) {
            breaches = breachRecordRepository.findByClientIdAndStatusOrderByCreatedAtDesc(clientId, status);
        } else {
            breaches = breachRecordRepository.findByClientIdOrderByCreatedAtDesc(
                    clientId, PageRequest.of(0, 1000)).getContent();
        }

        // Build CSV
        StringBuilder csv = new StringBuilder();
        csv.append("Domain,Breach Name,Date of Breach,Severity,Status,Recipients,Compromised Data\n");

        for (BreachRecord breach : breaches) {
            csv.append(breach.getDomain()).append(",");
            csv.append(breach.getBreachName()).append(",");
            csv.append(breach.getDateOfBreach() != null ? breach.getDateOfBreach().toString() : "").append(",");
            csv.append(breach.getSeverity()).append(",");
            csv.append(breach.getStatus()).append(",");
            csv.append(breach.getRecipientCount()).append(",");
            csv.append(String.join(";", breach.getCompromisedDataTypes())).append("\n");
        }

        return csv.toString().getBytes();
    }

    // ==================== Recipient Breach Operations ====================

    @Override
    public List<RecipientBreachDto> getRecipientBreaches(int offset, int pageSize,
            String breachRecordId, String keyword, RecipientBreachStatus status) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        PageRequest pageable = PageRequest.of(offset / pageSize, pageSize);

        Page<RecipientBreach> recipients;

        if (breachRecordId != null && !breachRecordId.isEmpty()) {
            recipients = recipientBreachRepository.findByBreachRecordIdOrderByCreatedAtDesc(
                    breachRecordId, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            recipients = recipientBreachRepository.searchRecipients(clientId, keyword, pageable);
        } else if (status != null) {
            recipients = recipientBreachRepository.findByClientIdAndStatusOrderByCreatedAtDesc(
                    clientId, status, pageable);
        } else {
            recipients = recipientBreachRepository.findByClientIdOrderByCreatedAtDesc(clientId, pageable);
        }

        return recipients.getContent().stream()
                .map(this::toRecipientBreachDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countRecipientBreaches(String breachRecordId, String keyword,
            RecipientBreachStatus status) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();

        if (breachRecordId != null && !breachRecordId.isEmpty()) {
            return recipientBreachRepository.countByBreachRecordId(breachRecordId);
        } else if (status != null) {
            return recipientBreachRepository.countByClientIdAndStatus(clientId, status);
        }
        return recipientBreachRepository.countByClientId(clientId);
    }

    @Override
    public RecipientBreachDto getRecipientBreachById(String id) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        RecipientBreach recipient = recipientBreachRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new RuntimeException("Recipient breach not found"));
        return toRecipientBreachDto(recipient);
    }

    @Override
    @Transactional
    public RecipientBreachDto notifyRecipient(String id, RecipientActionRequest request) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        String currentUser = userCurrentContextService.getCurrentUserContext().getUserId();

        RecipientBreach recipient = recipientBreachRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new RuntimeException("Recipient breach not found"));

        recipient.setStatus(RecipientBreachStatus.NOTIFIED);
        recipient.setNotifiedAt(Instant.now());
        recipient.addActionLog("NOTIFY", currentUser, request.getNotes());

        // TODO: Send actual notification email
        if (request.isSendEmail()) {
            log.info("Would send notification email to: {}", recipient.getEmail());
        }

        recipient = recipientBreachRepository.save(recipient);
        log.info("Notified recipient {} about breach", id);

        return toRecipientBreachDto(recipient);
    }

    @Override
    @Transactional
    public RecipientBreachDto resetRecipientPassword(String id, RecipientActionRequest request) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        String currentUser = userCurrentContextService.getCurrentUserContext().getUserId();

        RecipientBreach recipient = recipientBreachRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new RuntimeException("Recipient breach not found"));

        recipient.setPasswordResetAt(Instant.now());
        recipient.addActionLog("PASSWORD_RESET", currentUser, request.getNotes());

        // TODO: Trigger password reset via registration service
        log.info("Would trigger password reset for: {}", recipient.getEmail());

        recipient = recipientBreachRepository.save(recipient);
        log.info("Triggered password reset for recipient {}", id);

        return toRecipientBreachDto(recipient);
    }

    @Override
    @Transactional
    public RecipientBreachDto resolveRecipientBreach(String id, RecipientActionRequest request) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        String currentUser = userCurrentContextService.getCurrentUserContext().getUserId();

        RecipientBreach recipient = recipientBreachRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new RuntimeException("Recipient breach not found"));

        recipient.setStatus(RecipientBreachStatus.RESOLVED);
        recipient.setResolvedAt(Instant.now());
        recipient.addActionLog("RESOLVED", currentUser, request.getNotes());

        recipient = recipientBreachRepository.save(recipient);
        log.info("Resolved recipient breach {}", id);

        return toRecipientBreachDto(recipient);
    }

    // ==================== Configuration Operations ====================

    @Override
    public BreachConfigDto getConfig() {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();

        BreachDetectionConfig config = configRepository.findByClientId(clientId)
                .orElse(BreachDetectionConfig.builder()
                        .clientId(clientId)
                        .collectBreachData(true)
                        .monitoredDomains(new ArrayList<>())
                        .autoNotifyUsers(false)
                        .requirePasswordReset(false)
                        .syncIntervalHours(6)
                        .build());

        return toConfigDto(config);
    }

    @Override
    @Transactional
    public BreachConfigDto updateConfig(BreachConfigRequest request) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        String currentUser = userCurrentContextService.getCurrentUserContext().getUserId();

        BreachDetectionConfig config = configRepository.findByClientId(clientId)
                .orElse(BreachDetectionConfig.builder()
                        .clientId(clientId)
                        .build());

        config.setCollectBreachData(request.isCollectBreachData());
        config.setMonitoredDomains(request.getMonitoredDomains());
        config.setAutoNotifyUsers(request.isAutoNotifyUsers());
        config.setRequirePasswordReset(request.isRequirePasswordReset());
        config.setSyncIntervalHours(request.getSyncIntervalHours());
        config.setUpdatedBy(currentUser);

        config = configRepository.save(config);
        log.info("Updated breach detection config for client {}", clientId);

        return toConfigDto(config);
    }

    // ==================== Sync Operations ====================

    @Override
    @Transactional
    public BreachSyncResultDto triggerManualSync() {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        Instant startTime = Instant.now();

        log.info("Starting manual breach sync for client {}", clientId);

        BreachDetectionConfig config = configRepository.findByClientId(clientId)
                .orElse(null);

        if (config == null || !config.isCollectBreachData()) {
            return BreachSyncResultDto.builder()
                    .domainsProcessed(0)
                    .newBreachesFound(0)
                    .newRecipientsFound(0)
                    .errorsEncountered(0)
                    .syncStartedAt(startTime)
                    .syncCompletedAt(Instant.now())
                    .durationMs(0)
                    .status("SKIPPED")
                    .message("Breach collection is disabled")
                    .build();
        }

        List<String> domains = config.getMonitoredDomains();
        if (domains == null || domains.isEmpty()) {
            return BreachSyncResultDto.builder()
                    .domainsProcessed(0)
                    .newBreachesFound(0)
                    .newRecipientsFound(0)
                    .errorsEncountered(0)
                    .syncStartedAt(startTime)
                    .syncCompletedAt(Instant.now())
                    .durationMs(0)
                    .status("SKIPPED")
                    .message("No domains configured for monitoring")
                    .build();
        }

        InsecureWebClient.SyncResult result = insecureWebClient.syncAllDomains(domains);

        // Update last sync time
        config.setLastSyncAt(Instant.now());
        configRepository.save(config);

        Instant endTime = Instant.now();
        long duration = endTime.toEpochMilli() - startTime.toEpochMilli();

        return BreachSyncResultDto.builder()
                .domainsProcessed(result.getDomainsProcessed())
                .newBreachesFound(result.getNewBreachesFound())
                .newRecipientsFound(result.getNewRecipientsFound())
                .errorsEncountered(result.getErrorsEncountered())
                .syncStartedAt(startTime)
                .syncCompletedAt(endTime)
                .durationMs(duration)
                .status(result.isSuccess() ? "SUCCESS" : "PARTIAL")
                .message(result.isSuccess() ? "Sync completed successfully" :
                        "Sync completed with errors")
                .build();
    }

    // ==================== Mappers ====================

    private BreachRecordDto toBreachRecordDto(BreachRecord breach) {
        return BreachRecordDto.builder()
                .id(breach.getId())
                .domain(breach.getDomain())
                .breachName(breach.getBreachName())
                .dateOfBreach(breach.getDateOfBreach())
                .description(breach.getDescription())
                .compromisedDataTypes(breach.getCompromisedDataTypes())
                .recipientCount(breach.getRecipientCount())
                .severity(breach.getSeverity())
                .status(breach.getStatus())
                .sourceApi(breach.getSourceApi())
                .externalBreachId(breach.getExternalBreachId())
                .createdAt(breach.getCreatedAt())
                .updatedAt(breach.getUpdatedAt())
                .severityLabel(getSeverityLabel(breach.getSeverity()))
                .statusLabel(getStatusLabel(breach.getStatus()))
                .compromisedDataSummary(String.join(", ", breach.getCompromisedDataTypes()))
                .build();
    }

    private RecipientBreachDto toRecipientBreachDto(RecipientBreach recipient) {
        List<ActionLogDto> actionLogs = recipient.getActionLogs() != null ?
                recipient.getActionLogs().stream()
                        .map(this::toActionLogDto)
                        .collect(Collectors.toList()) : new ArrayList<>();

        return RecipientBreachDto.builder()
                .id(recipient.getId())
                .breachRecordId(recipient.getBreachRecordId())
                .userId(recipient.getUserId())
                .email(recipient.getEmail())
                .firstName(recipient.getFirstName())
                .lastName(recipient.getLastName())
                .fullName(recipient.getFullName())
                .tags(recipient.getTags())
                .breachCount(recipient.getBreachCount())
                .status(recipient.getStatus())
                .notifiedAt(recipient.getNotifiedAt())
                .passwordResetAt(recipient.getPasswordResetAt())
                .resolvedAt(recipient.getResolvedAt())
                .actionLogs(actionLogs)
                .createdAt(recipient.getCreatedAt())
                .statusLabel(getRecipientStatusLabel(recipient.getStatus()))
                .canNotify(recipient.getStatus() == RecipientBreachStatus.PENDING)
                .canResetPassword(recipient.getPasswordResetAt() == null)
                .canResolve(recipient.getStatus() != RecipientBreachStatus.RESOLVED)
                .build();
    }

    private ActionLogDto toActionLogDto(ActionLog log) {
        return ActionLogDto.builder()
                .action(log.getAction())
                .actionLabel(getActionLabel(log.getAction()))
                .performedBy(log.getPerformedBy())
                .performedAt(log.getPerformedAt())
                .notes(log.getNotes())
                .build();
    }

    private BreachConfigDto toConfigDto(BreachDetectionConfig config) {
        return BreachConfigDto.builder()
                .id(config.getId())
                .collectBreachData(config.isCollectBreachData())
                .monitoredDomains(config.getMonitoredDomains())
                .autoNotifyUsers(config.isAutoNotifyUsers())
                .requirePasswordReset(config.isRequirePasswordReset())
                .syncIntervalHours(config.getSyncIntervalHours())
                .lastSyncAt(config.getLastSyncAt())
                .updatedAt(config.getUpdatedAt())
                .updatedBy(config.getUpdatedBy())
                .build();
    }

    private String getSeverityLabel(BreachSeverity severity) {
        switch (severity) {
            case HIGH: return "High";
            case MEDIUM: return "Medium";
            case LOW: return "Low";
            default: return "Unknown";
        }
    }

    private String getStatusLabel(BreachStatus status) {
        switch (status) {
            case ACTION_REQUIRED: return "Action Required";
            case IN_PROGRESS: return "In Progress";
            case RESOLVED: return "Resolved";
            default: return "Unknown";
        }
    }

    private String getRecipientStatusLabel(RecipientBreachStatus status) {
        switch (status) {
            case PENDING: return "Pending";
            case NOTIFIED: return "Notified";
            case RESOLVED: return "Resolved";
            default: return "Unknown";
        }
    }

    private String getActionLabel(String action) {
        switch (action) {
            case "NOTIFY": return "User Notified";
            case "PASSWORD_RESET": return "Password Reset";
            case "MFA_ENABLED": return "MFA Enabled";
            case "RESOLVED": return "Resolved";
            default: return action;
        }
    }
}
