package com.aspire.asat.breachdetection.service.impl;

import com.aspire.asat.breachdetection.client.ShodanClient;
import com.aspire.asat.breachdetection.dto.AllResponseDto;
import com.aspire.asat.breachdetection.dto.enums.ImpersonationTactic;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertSeverity;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertStatus;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertType;
import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
import com.aspire.asat.breachdetection.dto.enums.SyncRunStatus;
import com.aspire.asat.breachdetection.dto.request.AddShodanMonitorRequest;
import com.aspire.asat.breachdetection.dto.response.ImpersonationTacticsSummaryDto;
import com.aspire.asat.breachdetection.dto.response.ShodanAlertDto;
import com.aspire.asat.breachdetection.dto.response.ShodanAlertTableRowDto;
import com.aspire.asat.breachdetection.dto.response.ShodanMonitorDto;
import com.aspire.asat.breachdetection.dto.response.ShodanOverviewDto;
import com.aspire.asat.breachdetection.dto.response.ShodanRiskTrendBucketDto;
import com.aspire.asat.breachdetection.dto.response.ShodanRiskTrendResponseDto;
import com.aspire.asat.breachdetection.dto.response.ShodanSyncResultDto;
import com.aspire.asat.breachdetection.dto.response.ShodanTacticDistributionDto;
import com.aspire.asat.breachdetection.model.ShodanAlert;
import com.aspire.asat.breachdetection.model.ShodanMonitor;
import com.aspire.asat.breachdetection.model.ShodanSyncRun;
import com.aspire.asat.breachdetection.repository.ShodanAlertRepository;
import com.aspire.asat.breachdetection.repository.ShodanMonitorRepository;
import com.aspire.asat.breachdetection.repository.ShodanSyncRunRepository;
import com.aspire.asat.breachdetection.service.ShodanBreachService;
import com.aspire.asat.breachdetection.utils.UserCurrentContextService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShodanBreachServiceImpl implements ShodanBreachService {

    private static final Set<Integer> ALLOWED_TREND_DAYS = Set.of(30, 60, 90);
    private static final int DEFAULT_TREND_DAYS = 30;
    private static final Set<String> MALICIOUS_TAGS = Set.of(
            "malware", "compromised", "botnet", "c2", "c&c", "trojan", "ransomware");
    private static final Set<String> PHISH_TAGS = Set.of("phish", "phishing", "honeypot", "scam", "fake");
    private static final Set<Integer> COMMON_PORTS = Set.of(80, 443, 22, 21, 25, 53, 110, 143, 465, 587, 993, 995);
    private static final Set<String> ALERT_SORT_FIELDS = Set.of(
            "dateDetected", "severity", "status", "alertType", "subject", "fakeSubject", "lastSeenAt");
    private static final Set<String> TRUSTED_PROVIDER_DOMAINS = Set.of(
            "outlook.com", "office365.com", "microsoft.com",
            "google.com", "googlemail.com", "gmail.com",
            "mailgun.org", "sendgrid.net", "amazonses.com",
            "cloudflare.net", "cloudfront.net", "awsdns.com", "awsdns.net", "awsdns.org", "awsdns.co.uk");
    private static final Set<String> COMMON_SECOND_LEVEL_TLDS = Set.of(
            "co.uk", "org.uk", "gov.uk", "ac.uk", "com.au", "net.au", "org.au", "co.nz");

    private final UserCurrentContextService userCurrentContextService;
    private final ShodanMonitorRepository monitorRepository;
    private final ShodanAlertRepository alertRepository;
    private final ShodanSyncRunRepository syncRunRepository;
    private final ShodanClient shodanClient;
    private final ObjectMapper objectMapper;

    // ------------------------------------------------------------------
    // Monitors
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public ShodanMonitorDto addMonitor(AddShodanMonitorRequest request) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        String currentUser = userCurrentContextService.getCurrentUserContext().getUserId();
        String subject = normalizeSubject(request.getSubjectType(), request.getSubject());

        ShodanMonitor monitor = monitorRepository
                .findByClientIdAndSubjectTypeAndSubject(clientId, request.getSubjectType(), subject)
                .orElseGet(() -> ShodanMonitor.builder()
                        .clientId(clientId)
                        .subjectType(request.getSubjectType())
                        .subject(subject)
                        .enabled(true)
                        .createdBy(currentUser)
                        .build());

        monitor.setEnabled(true);
        monitor.setNotes(request.getNotes());
        monitor.setUpdatedBy(currentUser);

        try {
            monitor = monitorRepository.save(monitor);
        } catch (DuplicateKeyException ex) {
            monitor = monitorRepository
                    .findByClientIdAndSubjectTypeAndSubject(clientId, request.getSubjectType(), subject)
                    .orElseThrow(() -> ex);
        }
        return toMonitorDto(monitor, clientId);
    }

    @Override
    @Transactional
    public void deleteMonitor(String id) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        monitorRepository.findByIdAndClientId(id, clientId).ifPresent(monitorRepository::delete);
    }

    @Override
    public List<ShodanMonitorDto> listMonitors(ShodanSubjectType subjectType) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        return monitorRepository.findByClientIdAndSubjectType(clientId, subjectType, PageRequest.of(0, 200))
                .stream()
                .map(m -> toMonitorDto(m, clientId))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // Sync
    // ------------------------------------------------------------------

    @Override
    @Transactional
    public ShodanSyncResultDto triggerManualSync(ShodanSubjectType subjectType) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        Instant startedAt = Instant.now();

        if (!shodanClient.isConfigured()) {
            return buildSkippedSync(subjectType, startedAt, "Shodan API is not configured");
        }

        List<ShodanMonitor> monitors =
                monitorRepository.findByClientIdAndSubjectTypeAndEnabledTrue(clientId, subjectType);
        if (monitors.isEmpty()) {
            return buildSkippedSync(subjectType, startedAt, "No monitored " + subjectType + "s for this client");
        }

        int alertsCreated = 0;
        int alertsUpdated = 0;
        int errorsEncountered = 0;
        List<String> errorMessages = new ArrayList<>();

        for (ShodanMonitor monitor : monitors) {
            try {
                List<ShodanAlert> produced = subjectType == ShodanSubjectType.IP
                        ? collectIpAlerts(clientId, monitor)
                        : collectDomainAlerts(clientId, monitor);
                for (ShodanAlert alert : produced) {
                    UpsertOutcome outcome = upsertAlert(clientId, alert);
                    if (outcome == UpsertOutcome.INSERTED) {
                        alertsCreated++;
                    } else if (outcome == UpsertOutcome.UPDATED) {
                        alertsUpdated++;
                    }
                }
                monitor.setLastSyncAt(Instant.now());
                monitorRepository.save(monitor);
            } catch (Exception ex) {
                errorsEncountered++;
                String msg = "Subject " + monitor.getSubject() + ": " + ex.getMessage();
                errorMessages.add(msg);
                log.error("Shodan sync error for {} {}: {}", subjectType, monitor.getSubject(), ex.getMessage(), ex);
            }
        }

        Instant completedAt = Instant.now();
        SyncRunStatus status;
        if (errorsEncountered == 0) {
            status = SyncRunStatus.SUCCESS;
        } else if (alertsCreated + alertsUpdated > 0) {
            status = SyncRunStatus.PARTIAL;
        } else {
            status = SyncRunStatus.FAILED;
        }

        syncRunRepository.save(ShodanSyncRun.builder()
                .clientId(clientId)
                .subjectType(subjectType)
                .startedAt(startedAt)
                .completedAt(completedAt)
                .status(status)
                .subjectsProcessed(monitors.size())
                .alertsCreated(alertsCreated)
                .alertsUpdated(alertsUpdated)
                .errorsEncountered(errorsEncountered)
                .errors(errorMessages)
                .build());

        return ShodanSyncResultDto.builder()
                .subjectType(subjectType)
                .subjectsProcessed(monitors.size())
                .alertsCreated(alertsCreated)
                .alertsUpdated(alertsUpdated)
                .errorsEncountered(errorsEncountered)
                .syncStartedAt(startedAt)
                .syncCompletedAt(completedAt)
                .durationMs(completedAt.toEpochMilli() - startedAt.toEpochMilli())
                .status(status)
                .errors(errorMessages)
                .message("Shodan " + subjectType.name().toLowerCase(Locale.ROOT) + " sync completed")
                .build();
    }

    private List<ShodanAlert> collectIpAlerts(String clientId, ShodanMonitor monitor) {
        ShodanClient.HostInfo host = shodanClient.getHostInformation(monitor.getSubject());
        String rawPayloadJson = toRawPayloadJson(host);
        Instant now = Instant.now();
        List<ShodanAlert> alerts = new ArrayList<>();

        // CVE vulnerabilities -> EXPLOIT_DATABASE
        for (String cve : host.getSafeVulns()) {
            alerts.add(buildBaseAlert(clientId, monitor, now, rawPayloadJson)
                    .alertType(ShodanAlertType.EXPLOIT_DATABASE)
                    .severity(severityForCve(cve))
                    .title("Vulnerability " + cve)
                    .description("Shodan reports " + cve + " affecting " + monitor.getSubject())
                    .source("Shodan /shodan/host/" + monitor.getSubject())
                    .fakeSubject(monitor.getSubject())
                    .evidence(List.of(cve))
                    .externalAlertId(externalAlertId(clientId, monitor, ShodanAlertType.EXPLOIT_DATABASE, cve))
                    .build());
        }

        // Top-level malicious tags -> BOTNET_LOG
        for (String tag : host.getSafeTags()) {
            if (matchesAny(tag, MALICIOUS_TAGS)) {
                alerts.add(buildBaseAlert(clientId, monitor, now, rawPayloadJson)
                        .alertType(ShodanAlertType.BOTNET_LOG)
                        .severity(ShodanAlertSeverity.HIGH)
                        .title("Malicious activity tag: " + tag)
                        .description("Shodan tagged " + monitor.getSubject() + " as '" + tag + "'")
                        .source("Shodan tags")
                        .fakeSubject(monitor.getSubject())
                        .evidence(List.of(tag))
                        .externalAlertId(externalAlertId(clientId, monitor, ShodanAlertType.BOTNET_LOG, "tag:" + tag))
                        .build());
            } else if (matchesAny(tag, PHISH_TAGS)) {
                alerts.add(buildBaseAlert(clientId, monitor, now, rawPayloadJson)
                        .alertType(ShodanAlertType.PHISHING_SITE)
                        .severity(ShodanAlertSeverity.HIGH)
                        .title("Phishing/honeypot tag: " + tag)
                        .description("Shodan tagged " + monitor.getSubject() + " as '" + tag + "'")
                        .source("Shodan tags")
                        .fakeSubject(monitor.getSubject())
                        .evidence(List.of(tag))
                        .externalAlertId(externalAlertId(clientId, monitor, ShodanAlertType.PHISHING_SITE, "tag:" + tag))
                        .build());
            }
        }

        // Suspicious exposed ports (non-common) -> EXPOSED_SERVICE (MEDIUM)
        // Group so one alert per IP, with evidence listing the ports.
        List<Integer> suspiciousPorts = host.getPorts() == null ? List.of() : host.getPorts().stream()
                .filter(p -> p != null && !COMMON_PORTS.contains(p))
                .distinct()
                .sorted()
                .toList();
        if (!suspiciousPorts.isEmpty()) {
            alerts.add(buildBaseAlert(clientId, monitor, now, rawPayloadJson)
                    .alertType(ShodanAlertType.EXPOSED_SERVICE)
                    .severity(ShodanAlertSeverity.MEDIUM)
                    .title("Unusual open ports: " + suspiciousPorts)
                    .description(suspiciousPorts.size() + " non-standard ports exposed on " + monitor.getSubject())
                    .source("Shodan ports")
                    .fakeSubject(monitor.getSubject())
                    .evidence(suspiciousPorts.stream().map(String::valueOf).toList())
                    .externalAlertId(externalAlertId(clientId, monitor, ShodanAlertType.EXPOSED_SERVICE, "ports"))
                    .build());
        }

        // Per-service vulns / tags also produce individual alerts
        for (ShodanClient.HostService svc : host.getSafeServices()) {
            for (String svcVuln : svc.getSafeVulns()) {
                String fp = "svc:" + svc.getPort() + ":" + svcVuln;
                alerts.add(buildBaseAlert(clientId, monitor, now, rawPayloadJson)
                        .alertType(ShodanAlertType.EXPLOIT_DATABASE)
                        .severity(severityForCve(svcVuln))
                        .title("Vulnerability " + svcVuln + " on port " + svc.getPort())
                        .description("Service " + defaultValue(svc.getProduct(), "unknown")
                                + " on port " + svc.getPort() + " is vulnerable to " + svcVuln)
                        .source("Shodan service")
                        .fakeSubject(monitor.getSubject())
                        .evidence(List.of(svcVuln, "port:" + svc.getPort()))
                        .externalAlertId(externalAlertId(clientId, monitor, ShodanAlertType.EXPLOIT_DATABASE, fp))
                        .build());
            }
        }

        return alerts;
    }

    private List<ShodanAlert> collectDomainAlerts(String clientId, ShodanMonitor monitor) {
        ShodanClient.DomainInfo info = shodanClient.getDomainInformation(monitor.getSubject());
        String rawPayloadJson = toRawPayloadJson(info);
        Instant now = Instant.now();
        List<ShodanAlert> alerts = new ArrayList<>();

        // Tag-driven classification
        for (String tag : info.getSafeTags()) {
            if (matchesAny(tag, PHISH_TAGS)) {
                alerts.add(buildBaseAlert(clientId, monitor, now, rawPayloadJson)
                        .alertType(ShodanAlertType.PHISHING_SITE)
                        .severity(ShodanAlertSeverity.HIGH)
                        .title("Phishing-related tag: " + tag)
                        .description("Shodan tagged " + monitor.getSubject() + " as '" + tag + "'")
                        .source("Shodan domain tags")
                        .fakeSubject(monitor.getSubject())
                        .evidence(List.of(tag))
                        .externalAlertId(externalAlertId(clientId, monitor, ShodanAlertType.PHISHING_SITE, "tag:" + tag))
                        .build());
            } else if (matchesAny(tag, MALICIOUS_TAGS)) {
                alerts.add(buildBaseAlert(clientId, monitor, now, rawPayloadJson)
                        .alertType(ShodanAlertType.BOTNET_LOG)
                        .severity(ShodanAlertSeverity.HIGH)
                        .title("Malicious tag: " + tag)
                        .description("Shodan tagged " + monitor.getSubject() + " as '" + tag + "'")
                        .source("Shodan domain tags")
                        .fakeSubject(monitor.getSubject())
                        .evidence(List.of(tag))
                        .externalAlertId(externalAlertId(clientId, monitor, ShodanAlertType.BOTNET_LOG, "tag:" + tag))
                        .build());
            }
        }

        // MX/CNAME records pointing away from our domain can indicate spoofing/impersonation.
        // Provider-hosted infrastructure (O365/Mailgun/etc.) is treated as expected unless
        // the registrable domain itself looks like a brand impersonation.
        for (ShodanClient.DnsRecord rec : info.getSafeRecords()) {
            String type = rec.getType() == null ? "" : rec.getType().toUpperCase(Locale.ROOT);
            if (!"MX".equals(type) && !"CNAME".equals(type)) {
                continue;
            }
            String target = normalizeDnsValue(rec.getValue());
            if (!StringUtils.hasText(target)) {
                continue;
            }
            if (isSameOrSubdomain(target, monitor.getSubject())) {
                continue;
            }
            boolean trustedProvider = isTrustedProviderTarget(target);
            boolean lookalike = looksLikeBrand(target, monitor.getSubject());
            if (trustedProvider && !lookalike) {
                continue;
            }

            ShodanAlertType alertType = "MX".equals(type)
                    ? ShodanAlertType.SPOOFED_EMAIL
                    : ShodanAlertType.TYPOSQUATTING_DOMAIN;
            ShodanAlertSeverity severity = lookalike ? ShodanAlertSeverity.HIGH : ShodanAlertSeverity.MEDIUM;

            alerts.add(buildBaseAlert(clientId, monitor, now, rawPayloadJson)
                    .alertType(alertType)
                    .severity(severity)
                    .title("Suspicious " + type + " target: " + target)
                    .description("Domain " + monitor.getSubject() + " has " + type + " record pointing to " + target
                            + (lookalike ? " (brand lookalike)" : ""))
                    .source("Shodan DNS (" + type + ")")
                    .fakeSubject(target)
                    .evidence(List.of(type + ":" + target, "trustedProvider:" + trustedProvider, "lookalike:" + lookalike))
                    .externalAlertId(externalAlertId(clientId, monitor, alertType, "dns:" + type.toLowerCase(Locale.ROOT) + ":" + target))
                    .build());
        }

        // Subdomain typosquat heuristic: subdomain label edit-distance <= 2 from the registered domain label
        String baseLabel = baseLabel(monitor.getSubject());
        for (String sub : info.getSafeSubdomains()) {
            if (sub == null || sub.isBlank() || "*".equals(sub)) {
                continue;
            }
            String leaf = lastLabel(sub);
            if (leaf.equalsIgnoreCase(baseLabel)) {
                continue;
            }
            int dist = levenshtein(leaf.toLowerCase(Locale.ROOT), baseLabel.toLowerCase(Locale.ROOT));
            if (dist > 0 && dist <= 2) {
                String fakeHost = sub + "." + monitor.getSubject();
                alerts.add(buildBaseAlert(clientId, monitor, now, rawPayloadJson)
                        .alertType(ShodanAlertType.TYPOSQUATTING_DOMAIN)
                        .severity(ShodanAlertSeverity.MEDIUM)
                        .title("Possible typosquat: " + fakeHost)
                        .description("Subdomain label '" + leaf + "' is " + dist
                                + " edit(s) away from '" + baseLabel + "'")
                        .source("Shodan DNS subdomains")
                        .fakeSubject(fakeHost)
                        .evidence(List.of(sub))
                        .externalAlertId(externalAlertId(clientId, monitor, ShodanAlertType.TYPOSQUATTING_DOMAIN, "sub:" + sub))
                        .build());
            }
        }

        return alerts;
    }

    private ShodanAlert.ShodanAlertBuilder buildBaseAlert(String clientId,
                                                          ShodanMonitor monitor,
                                                          Instant now,
                                                          String rawPayloadJson) {
        return ShodanAlert.builder()
                .clientId(clientId)
                .subjectType(monitor.getSubjectType())
                .subject(monitor.getSubject())
                .fakeSubject(monitor.getSubject())
                .status(ShodanAlertStatus.OPEN)
                .dateDetected(now)
                .firstSeenAt(now)
                .lastSeenAt(now)
                .rawPayloadJson(rawPayloadJson);
    }

    private UpsertOutcome upsertAlert(String clientId, ShodanAlert incoming) {
        if (incoming.getExternalAlertId() == null || incoming.getExternalAlertId().isBlank()) {
            return UpsertOutcome.SKIPPED;
        }
        Optional<ShodanAlert> existing =
                alertRepository.findByClientIdAndExternalAlertId(clientId, incoming.getExternalAlertId());
        if (existing.isPresent()) {
            ShodanAlert a = existing.get();
            a.setLastSeenAt(Instant.now());
            a.setSeverity(incoming.getSeverity());
            a.setAlertType(incoming.getAlertType());
            a.setTitle(incoming.getTitle());
            a.setDescription(incoming.getDescription());
            a.setSource(incoming.getSource());
            a.setFakeSubject(incoming.getFakeSubject());
            a.setEvidence(incoming.getEvidence());
            a.setRawPayloadJson(incoming.getRawPayloadJson());
            alertRepository.save(a);
            return UpsertOutcome.UPDATED;
        }
        alertRepository.save(incoming);
        return UpsertOutcome.INSERTED;
    }

    // ------------------------------------------------------------------
    // Read APIs
    // ------------------------------------------------------------------

    @Override
    public ShodanOverviewDto getOverview(ShodanSubjectType subjectType) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        long monitors = monitorRepository.countByClientIdAndSubjectType(clientId, subjectType);
        long total = alertRepository.countByClientIdAndSubjectType(clientId, subjectType);
        long open = alertRepository.countByClientIdAndSubjectTypeAndStatus(clientId, subjectType, ShodanAlertStatus.OPEN);
        long resolved = alertRepository.countByClientIdAndSubjectTypeAndStatus(clientId, subjectType, ShodanAlertStatus.RESOLVED);
        Instant lastScan = syncRunRepository.findTopByClientIdAndSubjectTypeOrderByStartedAtDesc(clientId, subjectType)
                .map(ShodanSyncRun::getStartedAt)
                .orElse(null);
        return ShodanOverviewDto.builder()
                .subjectType(subjectType)
                .subjectsMonitored(monitors)
                .totalBreaches(total)
                .openBreaches(open)
                .resolvedBreaches(resolved)
                .lastScanAt(lastScan)
                .build();
    }

    @Override
    public AllResponseDto<List<ShodanAlertDto>> getAlerts(ShodanSubjectType subjectType,
                                                          int offset,
                                                          int pageSize,
                                                          String sortBy,
                                                          String sortDirection,
                                                          ShodanAlertStatus status,
                                                          ShodanAlertSeverity severity,
                                                          ShodanAlertType alertType) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        int safeOffset = Math.max(offset, 0);
        int safePageSize = Math.max(pageSize, 1);
        PageRequest pageable = buildAlertPageable(safeOffset, safePageSize, sortBy, sortDirection);
        Page<ShodanAlert> pageResult = queryAlerts(clientId, subjectType, status, severity, alertType, pageable);
        return new AllResponseDto<>(
                safeOffset,
                safePageSize,
                pageResult.getTotalElements(),
                pageResult.getContent().stream().map(this::toAlertDto).collect(Collectors.toList())
        );
    }

    @Override
    public AllResponseDto<List<ShodanAlertTableRowDto>> getAlertsTable(ShodanSubjectType subjectType,
                                                                       int offset,
                                                                       int pageSize,
                                                                       String sortBy,
                                                                       String sortDirection,
                                                                       ShodanAlertStatus status,
                                                                       ShodanAlertSeverity severity,
                                                                       ShodanAlertType alertType) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        int safeOffset = Math.max(offset, 0);
        int safePageSize = Math.max(pageSize, 1);
        PageRequest pageable = buildAlertPageable(safeOffset, safePageSize, sortBy, sortDirection);
        Page<ShodanAlert> pageResult = queryAlerts(clientId, subjectType, status, severity, alertType, pageable);
        return new AllResponseDto<>(
                safeOffset,
                safePageSize,
                pageResult.getTotalElements(),
                pageResult.getContent().stream().map(this::toAlertTableRowDto).collect(Collectors.toList())
        );
    }

    @Override
    public ShodanAlertDto getAlertById(String id) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        ShodanAlert alert = alertRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        return toAlertDto(alert);
    }

    @Override
    @Transactional
    public ShodanAlertDto updateAlertStatus(String id, ShodanAlertStatus status) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        ShodanAlert alert = alertRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new RuntimeException("Alert not found"));
        alert.setStatus(status);
        return toAlertDto(alertRepository.save(alert));
    }

    @Override
    public ShodanRiskTrendResponseDto getRiskTrend(ShodanSubjectType subjectType, int days) {
        int windowDays = ALLOWED_TREND_DAYS.contains(days) ? days : DEFAULT_TREND_DAYS;
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate fromDate = today.minusDays((long) windowDays - 1);
        Instant fromInstant = fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        TreeMap<LocalDate, ShodanRiskTrendBucketDto> buckets = new TreeMap<>();
        for (int i = 0; i < windowDays; i++) {
            LocalDate day = fromDate.plusDays(i);
            buckets.put(day, ShodanRiskTrendBucketDto.builder().date(day).build());
        }

        List<ShodanAlert> alerts = alertRepository.findInRange(clientId, subjectType, fromInstant, toInstant);

        long totalLow = 0, totalMedium = 0, totalHigh = 0, totalCritical = 0;
        for (ShodanAlert alert : alerts) {
            if (alert.getDateDetected() == null) {
                continue;
            }
            LocalDate day = alert.getDateDetected().atZone(ZoneOffset.UTC).toLocalDate();
            ShodanRiskTrendBucketDto bucket = buckets.get(day);
            if (bucket == null) {
                continue;
            }
            ShodanAlertSeverity sev = alert.getSeverity() == null ? ShodanAlertSeverity.LOW : alert.getSeverity();
            switch (sev) {
                case CRITICAL -> { bucket.setCritical(bucket.getCritical() + 1); totalCritical++; }
                case HIGH -> { bucket.setHigh(bucket.getHigh() + 1); totalHigh++; }
                case MEDIUM -> { bucket.setMedium(bucket.getMedium() + 1); totalMedium++; }
                default -> { bucket.setLow(bucket.getLow() + 1); totalLow++; }
            }
            bucket.setTotal(bucket.getTotal() + 1);
        }

        return ShodanRiskTrendResponseDto.builder()
                .subjectType(subjectType)
                .days(windowDays)
                .from(fromDate)
                .to(today)
                .buckets(new ArrayList<>(buckets.values()))
                .totalLow(totalLow)
                .totalMedium(totalMedium)
                .totalHigh(totalHigh)
                .totalCritical(totalCritical)
                .total(totalLow + totalMedium + totalHigh + totalCritical)
                .build();
    }

    @Override
    public ShodanTacticDistributionDto getTacticDistribution(ShodanSubjectType subjectType) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        List<ShodanAlert> alerts = alertRepository.findForTacticDistribution(clientId, subjectType);

        Map<ImpersonationTactic, Long> counts = new EnumMap<>(ImpersonationTactic.class);
        for (ImpersonationTactic t : ImpersonationTactic.values()) {
            counts.put(t, 0L);
        }
        long total = 0;
        for (ShodanAlert alert : alerts) {
            if (alert.getAlertType() == null) {
                continue;
            }
            ImpersonationTactic tactic = alert.getAlertType().getTactic();
            counts.merge(tactic, 1L, Long::sum);
            total++;
        }

        long totalForPct = total;
        List<ShodanTacticDistributionDto.Slice> slices = counts.entrySet().stream()
                .map(e -> ShodanTacticDistributionDto.Slice.builder()
                        .tactic(e.getKey())
                        .count(e.getValue())
                        .percentage(totalForPct == 0
                                ? 0.0
                                : Math.round((e.getValue() * 10000.0) / totalForPct) / 100.0)
                        .build())
                .collect(Collectors.toList());

        return ShodanTacticDistributionDto.builder()
                .subjectType(subjectType)
                .total(total)
                .slices(slices)
                .build();
    }

    @Override
    public ImpersonationTacticsSummaryDto getImpersonationTactics(ShodanSubjectType subjectType) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        return ImpersonationTacticsSummaryDto.builder()
                .subjectType(subjectType)
                .phishingWebsites(alertRepository.countByClientIdAndSubjectTypeAndAlertType(
                        clientId, subjectType, ShodanAlertType.PHISHING_SITE))
                .spoofedEmails(alertRepository.countByClientIdAndSubjectTypeAndAlertType(
                        clientId, subjectType, ShodanAlertType.SPOOFED_EMAIL))
                .typosquattingDomains(alertRepository.countByClientIdAndSubjectTypeAndAlertType(
                        clientId, subjectType, ShodanAlertType.TYPOSQUATTING_DOMAIN))
                .fakeSocialProfiles(alertRepository.countByClientIdAndSubjectTypeAndAlertType(
                        clientId, subjectType, ShodanAlertType.FAKE_SOCIAL_PROFILE))
                .build();
    }

    // ------------------------------------------------------------------
    // Mapping & helpers
    // ------------------------------------------------------------------

    private ShodanMonitorDto toMonitorDto(ShodanMonitor monitor, String clientId) {
        long openAlerts = alertRepository.countByClientIdAndSubjectAndStatus(
                clientId, monitor.getSubject(), ShodanAlertStatus.OPEN);
        return ShodanMonitorDto.builder()
                .id(monitor.getId())
                .subjectType(monitor.getSubjectType())
                .subject(monitor.getSubject())
                .enabled(monitor.isEnabled())
                .notes(monitor.getNotes())
                .lastSyncAt(monitor.getLastSyncAt())
                .openAlerts(openAlerts)
                .createdAt(monitor.getCreatedAt())
                .updatedAt(monitor.getUpdatedAt())
                .build();
    }

    private ShodanAlertDto toAlertDto(ShodanAlert alert) {
        return ShodanAlertDto.builder()
                .id(alert.getId())
                .subjectType(alert.getSubjectType())
                .subject(alert.getSubject())
                .fakeSubject(alert.getFakeSubject())
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .dateDetected(alert.getDateDetected())
                .lastSeenAt(alert.getLastSeenAt())
                .title(alert.getTitle())
                .description(alert.getDescription())
                .source(alert.getSource())
                .evidence(alert.getEvidence())
                .build();
    }

    private ShodanAlertTableRowDto toAlertTableRowDto(ShodanAlert alert) {
        String fakeDomain = StringUtils.hasText(alert.getFakeSubject()) ? alert.getFakeSubject() : alert.getSubject();
        return ShodanAlertTableRowDto.builder()
                .id(alert.getId())
                .subject(alert.getSubject())
                .fakeDomain(fakeDomain)
                .type(alert.getAlertType())
                .dateDetected(alert.getDateDetected())
                .severity(alert.getSeverity())
                .status(alert.getStatus())
                .build();
    }

    private Page<ShodanAlert> queryAlerts(String clientId,
                                          ShodanSubjectType subjectType,
                                          ShodanAlertStatus status,
                                          ShodanAlertSeverity severity,
                                          ShodanAlertType alertType,
                                          PageRequest pageable) {
        if (status != null) {
            return alertRepository.findByClientIdAndSubjectTypeAndStatus(
                    clientId, subjectType, status, pageable);
        }
        if (severity != null) {
            return alertRepository.findByClientIdAndSubjectTypeAndSeverity(
                    clientId, subjectType, severity, pageable);
        }
        if (alertType != null) {
            return alertRepository.findByClientIdAndSubjectTypeAndAlertType(
                    clientId, subjectType, alertType, pageable);
        }
        return alertRepository.findByClientIdAndSubjectType(
                clientId, subjectType, pageable);
    }

    private PageRequest buildAlertPageable(int offset, int pageSize, String sortBy, String sortDirection) {
        String safeSortBy = ALERT_SORT_FIELDS.contains(sortBy) ? sortBy : "dateDetected";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(offset, pageSize, Sort.by(direction, safeSortBy));
    }

    private ShodanSyncResultDto buildSkippedSync(ShodanSubjectType subjectType, Instant startedAt, String message) {
        Instant completedAt = Instant.now();
        return ShodanSyncResultDto.builder()
                .subjectType(subjectType)
                .subjectsProcessed(0)
                .alertsCreated(0)
                .alertsUpdated(0)
                .errorsEncountered(0)
                .syncStartedAt(startedAt)
                .syncCompletedAt(completedAt)
                .durationMs(completedAt.toEpochMilli() - startedAt.toEpochMilli())
                .status(SyncRunStatus.SKIPPED)
                .message(message)
                .build();
    }

    private String normalizeSubject(ShodanSubjectType subjectType, String subject) {
        if (!StringUtils.hasText(subject)) {
            throw new IllegalArgumentException("subject is required");
        }
        String normalized = subject.trim().toLowerCase(Locale.ROOT);
        if (subjectType == ShodanSubjectType.DOMAIN) {
            if (normalized.startsWith("http://")) {
                normalized = normalized.substring(7);
            } else if (normalized.startsWith("https://")) {
                normalized = normalized.substring(8);
            }
            int slash = normalized.indexOf('/');
            if (slash > 0) {
                normalized = normalized.substring(0, slash);
            }
        }
        return normalized;
    }

    private ShodanAlertSeverity severityForCve(String cve) {
        // Without CVSS data in the response, choose HIGH for explicit CVE-IDs; MEDIUM otherwise.
        if (cve != null && cve.toUpperCase(Locale.ROOT).startsWith("CVE-")) {
            return ShodanAlertSeverity.HIGH;
        }
        return ShodanAlertSeverity.MEDIUM;
    }

    private boolean matchesAny(String value, Set<String> needles) {
        if (!StringUtils.hasText(value)) {
            return false;
        }
        String lower = value.toLowerCase(Locale.ROOT);
        for (String n : needles) {
            if (lower.contains(n)) {
                return true;
            }
        }
        return false;
    }

    private String externalAlertId(String clientId, ShodanMonitor monitor, ShodanAlertType alertType, String fingerprint) {
        return String.join("|", clientId, monitor.getSubjectType().name(), monitor.getSubject(),
                alertType.name(), safe(fingerprint));
    }

    private String safe(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String defaultValue(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String normalizeDnsValue(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String v = value.trim().toLowerCase(Locale.ROOT);
        while (v.endsWith(".")) {
            v = v.substring(0, v.length() - 1);
        }
        return v;
    }

    private boolean isSameOrSubdomain(String candidate, String monitoredDomain) {
        if (!StringUtils.hasText(candidate) || !StringUtils.hasText(monitoredDomain)) {
            return false;
        }
        String c = normalizeDnsValue(candidate);
        String d = normalizeDnsValue(monitoredDomain);
        assert c != null;
        return c.equals(d) || c.endsWith("." + d);
    }

    private boolean isTrustedProviderTarget(String host) {
        String registrable = extractRegistrableDomain(host);
        return TRUSTED_PROVIDER_DOMAINS.contains(registrable);
    }

    private boolean looksLikeBrand(String candidateHost, String monitoredDomain) {
        String brand = normalizeLabel(baseLabel(monitoredDomain));
        String candidate = normalizeLabel(baseLabel(extractRegistrableDomain(candidateHost)));
        if (!StringUtils.hasText(brand) || !StringUtils.hasText(candidate) || brand.equals(candidate)) {
            return false;
        }
        if (candidate.contains(brand) || brand.contains(candidate)) {
            return true;
        }
        int dist = levenshtein(candidate, brand);
        return dist > 0 && dist <= 2;
    }

    private String extractRegistrableDomain(String host) {
        String normalized = normalizeDnsValue(host);
        if (!StringUtils.hasText(normalized)) {
            return "";
        }
        String[] parts = normalized.split("\\.");
        if (parts.length <= 2) {
            return normalized;
        }
        String tail2 = parts[parts.length - 2] + "." + parts[parts.length - 1];
        if (COMMON_SECOND_LEVEL_TLDS.contains(tail2) && parts.length >= 3) {
            return parts[parts.length - 3] + "." + tail2;
        }
        return tail2;
    }

    private String normalizeLabel(String label) {
        if (!StringUtils.hasText(label)) {
            return "";
        }
        return label.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private String toRawPayloadJson(Object payload) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize Shodan payload for storage: {}", ex.getMessage());
            return null;
        }
    }

    private String baseLabel(String domain) {
        if (!StringUtils.hasText(domain)) {
            return "";
        }
        String[] parts = domain.split("\\.");
        return parts.length >= 2 ? parts[parts.length - 2] : parts[0];
    }

    private String lastLabel(String subdomain) {
        if (!StringUtils.hasText(subdomain)) {
            return "";
        }
        String trimmed = subdomain;
        if (trimmed.startsWith("*.")) {
            trimmed = trimmed.substring(2);
        }
        String[] parts = trimmed.split("\\.");
        return parts.length == 0 ? "" : parts[parts.length - 1];
    }

    private int levenshtein(String a, String b) {
        if (a == null) {
            a = "";
        }
        if (b == null) {
            b = "";
        }
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(curr[j - 1] + 1, prev[j] + 1), prev[j - 1] + cost);
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return prev[b.length()];
    }

    private enum UpsertOutcome { INSERTED, UPDATED, SKIPPED }
}
