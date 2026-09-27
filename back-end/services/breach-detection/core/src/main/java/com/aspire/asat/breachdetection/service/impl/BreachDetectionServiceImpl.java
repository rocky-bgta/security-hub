package com.aspire.asat.breachdetection.service.impl;

import com.aspire.asat.breachdetection.client.InsecureWebClient;
import com.aspire.asat.breachdetection.client.RegistrationServiceClient;
import com.aspire.asat.breachdetection.dto.AllResponseDto;
import com.aspire.asat.breachdetection.dto.enums.BreachSeverity;
import com.aspire.asat.breachdetection.dto.enums.InsecureWebBreachStatus;
import com.aspire.asat.breachdetection.dto.enums.SyncRunStatus;
import com.aspire.asat.breachdetection.dto.request.BreachConfigRequest;
import com.aspire.asat.breachdetection.dto.request.InsecureWebBreachSearchRequest;
import com.aspire.asat.breachdetection.dto.response.BreachActivityBucketDto;
import com.aspire.asat.breachdetection.dto.response.BreachActivityResponseDto;
import com.aspire.asat.breachdetection.dto.response.BreachConfigDto;
import com.aspire.asat.breachdetection.dto.response.BreachSyncResultDto;
import com.aspire.asat.breachdetection.dto.response.InsecureWebBreachFindingDto;
import com.aspire.asat.breachdetection.dto.response.InsecureWebFindingsSummaryDto;
import com.aspire.asat.breachdetection.model.BreachDetectionConfig;
import com.aspire.asat.breachdetection.model.InsecureWebBreachFinding;
import com.aspire.asat.breachdetection.model.InsecureWebOrganization;
import com.aspire.asat.breachdetection.model.InsecureWebSyncRun;
import com.aspire.asat.breachdetection.repository.BreachDetectionConfigRepository;
import com.aspire.asat.breachdetection.repository.InsecureWebBreachFindingRepository;
import com.aspire.asat.breachdetection.repository.InsecureWebOrganizationRepository;
import com.aspire.asat.breachdetection.repository.InsecureWebSyncRunRepository;
import com.aspire.asat.breachdetection.service.BreachDetectionService;
import com.aspire.asat.breachdetection.utils.BreachSeverityClassifier;
import com.aspire.asat.breachdetection.utils.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BreachDetectionServiceImpl implements BreachDetectionService {

    private final UserCurrentContextService userCurrentContextService;
    private final BreachDetectionConfigRepository configRepository;
    private final InsecureWebOrganizationRepository organizationRepository;
    private final InsecureWebBreachFindingRepository findingRepository;
    private final InsecureWebSyncRunRepository syncRunRepository;
    private final RegistrationServiceClient registrationServiceClient;
    private final InsecureWebClient insecureWebClient;
    private final BreachSeverityClassifier severityClassifier;
    private final MongoTemplate mongoTemplate;

    private static final Set<Integer> ALLOWED_ACTIVITY_DAYS = Set.of(30, 60, 90);
    private static final int DEFAULT_ACTIVITY_DAYS = 30;
    private static final Set<String> FINDING_SORT_FIELDS = Set.of(
            "timestamp", "domain", "email", "source", "breachStatus", "createdAt", "updatedAt", "lastSeenAt");

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
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        String clientId = context.getClientAdminId();
        String currentUser = context.getUserId();
        BreachDetectionConfig config = configRepository.findByClientId(clientId)
                .orElse(BreachDetectionConfig.builder().clientId(clientId).build());


        config.setCollectBreachData(request.isCollectBreachData());
        if (request.getEmail() != null) {
            config.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        }
        config.setMonitoredDomains(extractDomains(context.getEmail(), request.getMonitoredDomains()));
        config.setAutoNotifyUsers(request.isAutoNotifyUsers());
        config.setRequirePasswordReset(request.isRequirePasswordReset());
        config.setSyncIntervalHours(request.getSyncIntervalHours());
        config.setUpdatedBy(currentUser);

        return toConfigDto(configRepository.save(config));
    }

    @Override
    @Transactional
    public BreachSyncResultDto triggerManualSync() {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        BreachDetectionConfig config = configRepository.findByClientId(clientId).orElse(null);
        return syncClientEmails(clientId, config, true);
    }

    @Override
    @Transactional
    public void triggerScheduledSync(BreachDetectionConfig config) {
        if (config == null) {
            buildSkippedResult("Breach detection configuration not found", Instant.now());
            return;
        }
        syncClientEmails(config.getClientId(), config, false);
    }

    private BreachSyncResultDto syncClientEmails(String clientId, BreachDetectionConfig config, boolean manual) {
        Instant startTime = Instant.now();
        if (config == null || !config.isCollectBreachData()) {
            return buildSkippedResult("Breach collection is disabled", startTime);
        }
        if (!insecureWebClient.isConfigured()) {
            return buildSkippedResult("InsecureWeb API is not configured", startTime);
        }

        List<RegistrationServiceClient.UserDto> users = Optional.ofNullable(
                registrationServiceClient.getAllUsers(clientId)).orElseGet(List::of);

        log.info("Fetched {} users from registration service for client {}", users.size(), clientId);
        log.info("User details: {}", users.stream()
                .filter(Objects::nonNull)
                .map(user -> String.format("userId=%s, email=%s", user.getUserId(), user.getEmail()))
                .toList());

        List<String> emailsToCheck = new ArrayList<>(users.stream()
                .filter(Objects::nonNull)
                .map(RegistrationServiceClient.UserDto::getEmail)
                .filter(Objects::nonNull)
                .map(this::normalizeEmail)
                .distinct()
                .toList());

        // Add the Admin email as well if not present in the users list
        emailsToCheck.add(config.getEmail());

        int inserted = 0;
        int updated = 0;
        int errorsEncountered = 0;
        int recordsFetched = 0;
        int finalPage = 0;
        List<String> errorMessages = new ArrayList<>();

        InsecureWebOrganization organization = ensureOrganization(clientId, config, users, emailsToCheck);

        int page = 0;
        final int size = 1000;
        boolean last = false;
        while (!last) {
            try {
                InsecureWebClient.PagedBreachesResponse response = insecureWebClient.getOrganizationBreaches(
                        organization.getInsecureWebOrganizationId(), true, true, page, size);
                List<InsecureWebClient.OrganizationBreachItem> items = response.getItems();
                if (items.isEmpty()) {
                    break;
                }
                recordsFetched += items.size();
                for (InsecureWebClient.OrganizationBreachItem item : items) {
                    UpsertOutcome outcome = upsertFinding(clientId, organization.getInsecureWebOrganizationId(), item);
                    if (outcome.inserted) {
                        inserted++;
                    } else if (outcome.updated) {
                        updated++;
                    }
                }
                finalPage = page;
                last = response.isLast() || (response.getTotalPages() > 0 && page >= response.getTotalPages() - 1);
                page++;
            } catch (Exception ex) {
                errorsEncountered++;
                errorMessages.add(ex.getMessage());
                log.error("Error syncing organization {} page {}: {}", organization.getInsecureWebOrganizationId(),
                        page, ex.getMessage(), ex);
                last = true;
            }
        }

        Instant endTime = Instant.now();
        config.setLastSyncAt(endTime);
        configRepository.save(config);
        organization.setLastSyncAt(endTime);
        organizationRepository.save(organization);

        SyncRunStatus status;
        if (errorsEncountered == 0) {
            status = SyncRunStatus.SUCCESS;
        } else if (recordsFetched > 0) {
            status = SyncRunStatus.PARTIAL;
        } else {
            status = SyncRunStatus.FAILED;
        }

        syncRunRepository.save(InsecureWebSyncRun.builder()
                .clientId(clientId)
                .organizationId(organization.getInsecureWebOrganizationId())
                .startedAt(startTime)
                .completedAt(endTime)
                .status(status)
                .pageStart(0)
                .pageEnd(finalPage)
                .recordsFetched(recordsFetched)
                .recordsInserted(inserted)
                .recordsUpdated(updated)
                .errors(errorMessages)
                .build());

        return BreachSyncResultDto.builder()
                .domainsProcessed(recordsFetched)
                .newBreachesFound(inserted)
                .newRecipientsFound(updated)
                .errorsEncountered(errorsEncountered)
                .syncStartedAt(startTime)
                .syncCompletedAt(endTime)
                .durationMs(endTime.toEpochMilli() - startTime.toEpochMilli())
                .status(status)
                .message(manual ? "Manual organization breach sync completed" : "Scheduled organization breach sync completed")
                .build();
    }


    private Optional<InsecureWebOrganization> findExistingOrganization(String clientId, BreachDetectionConfig config) {
        Optional<InsecureWebOrganization> byClientId = organizationRepository.findByClientId(clientId);
        if (byClientId.isPresent()) {
            return byClientId;
        }
        if (CollectionUtils.isEmpty(config.getMonitoredDomains())) {
            return Optional.empty();
        }
        List<InsecureWebOrganization> byDomains = organizationRepository.findByDomainsIn(config.getMonitoredDomains());
        return byDomains.stream()
                .max(Comparator.comparing(InsecureWebOrganization::getLastSyncAt,
                        Comparator.nullsLast(Comparator.naturalOrder())));
    }

    private InsecureWebOrganization ensureOrganization(String clientId,
                                                       BreachDetectionConfig config,
                                                       List<RegistrationServiceClient.UserDto> users,
                                                       List<String> emails) {
        Optional<InsecureWebOrganization> byClient = findExistingOrganization(clientId, config);
        if (byClient.isPresent()) {
            return byClient.get();
        }
        List<String> domains = Optional.ofNullable(config.getMonitoredDomains()).orElseGet(ArrayList::new);
        List<String> usernames = users.stream()
                .map(RegistrationServiceClient.UserDto::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        InsecureWebClient.ExternalOrganizationDto externalOrg = insecureWebClient.createOrganization(
                InsecureWebClient.CreateOrganizationRequest.builder()
                        .orgName("asat-client-" + clientId)
                        .orgDescription("ASAT breach monitoring org for client " + clientId)
                        .domains(domains)
                        .emails(emails)
                        .users(usernames)
                        .ips(List.of())
                        .phones(List.of())
                        .scanServices(List.of("DARK_WEB"))
                        .build()
        );
        InsecureWebOrganization organization = InsecureWebOrganization.builder()
                .clientId(clientId)
                .insecureWebOrganizationId(externalOrg.getId())
                .orgName(externalOrg.getOrgName())
                .orgDescription(externalOrg.getOrgDescription())
                .domains(Optional.ofNullable(externalOrg.getDomains()).orElseGet(ArrayList::new))
                .emails(Optional.ofNullable(externalOrg.getEmails()).orElseGet(ArrayList::new))
                .users(Optional.ofNullable(externalOrg.getUsers()).orElseGet(ArrayList::new))
                .ips(Optional.ofNullable(externalOrg.getIps()).orElseGet(ArrayList::new))
                .phones(Optional.ofNullable(externalOrg.getPhones()).orElseGet(ArrayList::new))
                .scanServices(Optional.ofNullable(externalOrg.getScanServices()).orElseGet(ArrayList::new))
                .syncEnabled(true)
                .build();
        return organizationRepository.save(organization);
    }

    private UpsertOutcome upsertFinding(String clientId, long organizationId, InsecureWebClient.OrganizationBreachItem item) {
        String externalFindingId = item.getId();
        if (externalFindingId == null || externalFindingId.isBlank()) {
            return UpsertOutcome.none();
        }
        Instant now = Instant.now();
        Optional<InsecureWebBreachFinding> existing = findingRepository.findByClientIdAndExternalFindingId(clientId, externalFindingId);
        if (existing.isPresent()) {
            InsecureWebBreachFinding finding = existing.get();
            mapItemToFinding(finding, organizationId, item);
            finding.setLastSeenAt(now);
            findingRepository.save(finding);
            return UpsertOutcome.updated();
        }
        InsecureWebBreachFinding finding = InsecureWebBreachFinding.builder()
                .clientId(clientId)
                .externalFindingId(externalFindingId)
                .organizationId(organizationId)
                .firstSeenAt(now)
                .lastSeenAt(now)
                .build();
        mapItemToFinding(finding, organizationId, item);
        findingRepository.save(finding);
        return UpsertOutcome.inserted();
    }

    private void mapItemToFinding(InsecureWebBreachFinding finding, long organizationId, InsecureWebClient.OrganizationBreachItem item) {
        finding.setOrganizationId(organizationId);
        finding.setTimestamp(item.getTimestamp());
        finding.setDomain(item.getDomain());
        finding.setEmail(item.getEmail());
        finding.setIpAddress(item.getIpAddress());
        finding.setUsername(item.getUsername());
        finding.setPassword(item.getPassword());
        finding.setHashedPassword(item.getHashedPassword());
        finding.setPhone(item.getPhone());
        finding.setDatabaseName(item.getDatabaseName());
        finding.setFoundIn(item.getFoundIn());
        finding.setSource(item.getSource());
        finding.setLeakName(item.getLeakName());
        finding.setBreachDescription(item.getBreachDescription());
        finding.setCompromisedData(item.getCompromisedData());
        finding.setVictimDomain(item.getVictimDomain());
        finding.setOrganizationElement(item.getOrganizationElement());
        finding.setOrganizationElementType(item.getOrganizationElementType());
        finding.setBreachStatus(item.getBreachStatus() == null ? InsecureWebBreachStatus.UNKNOWN : item.getBreachStatus());
        finding.setEmployee(item.getEmployee());
        finding.setEchoesCount(item.getEchoesCount());
        finding.setRawPayloadJson(item.getRawJson());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private List<String> extractDomains(String email, List<String> monitoredDomains) {

        if (!CollectionUtils.isEmpty(monitoredDomains)) {
            return monitoredDomains;
        }

        if (!StringUtils.hasText(email)) {
            return Collections.emptyList();
        }
        String normalized = normalizeEmail(email);
        int atIndex = normalized.lastIndexOf('@');
        if (atIndex < 0 || atIndex == normalized.length() - 1) {
            return Collections.emptyList();
        }
        String domain = normalized.substring(atIndex + 1).trim();
        return List.of(domain);
    }

    private BreachSyncResultDto buildSkippedResult(String message, Instant startTime) {
        Instant endTime = Instant.now();
        return BreachSyncResultDto.builder()
                .domainsProcessed(0)
                .newBreachesFound(0)
                .newRecipientsFound(0)
                .errorsEncountered(0)
                .syncStartedAt(startTime)
                .syncCompletedAt(endTime)
                .durationMs(endTime.toEpochMilli() - startTime.toEpochMilli())
                .status(SyncRunStatus.SKIPPED)
                .message(message)
                .build();
    }

    @Override
    public AllResponseDto<List<InsecureWebBreachFindingDto>> getFindings(
            int offset, int pageSize, String sortBy, String sortDirection,
            String fromDate, String toDate, String email, String domain, InsecureWebBreachStatus breachStatus) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        int safeOffset = Math.max(offset, 0);
        int safePageSize = Math.max(pageSize, 1);
        String safeSortBy = FINDING_SORT_FIELDS.contains(sortBy) ? sortBy : "timestamp";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        DateRange dateRange = resolveDateRange(fromDate, toDate);
        Query query = new Query();
        // TODO: TESTING ONLY - clientId filter temporarily disabled to return all data. Restore before production.
        // query.addCriteria(Criteria.where("clientId").is(clientId));
        if (StringUtils.hasText(email)) {
            query.addCriteria(Criteria.where("email")
                    .regex(Pattern.quote(email.trim()), "i"));
        }
        if (StringUtils.hasText(domain)) {
            query.addCriteria(Criteria.where("domain")
                    .regex(Pattern.quote(domain.trim()), "i"));
        }
        if (breachStatus != null) {
            query.addCriteria(Criteria.where("breachStatus").is(breachStatus));
        }
        if (dateRange.isActive()) {
            query.addCriteria(Criteria.where("timestamp")
                    .gte(dateRange.fromInclusive())
                    .lt(dateRange.toExclusive()));
        }

        long total = mongoTemplate.count(query, InsecureWebBreachFinding.class);
        query.with(Sort.by(direction, safeSortBy));
        query.skip((long) safeOffset * safePageSize);
        query.limit(safePageSize);

        List<InsecureWebBreachFinding> findings = mongoTemplate.find(query, InsecureWebBreachFinding.class);
        return new AllResponseDto<>(safeOffset, safePageSize, total,
                findings
                        .stream()
                        .map(this::toFindingDto)
                        .toList());
    }

    @Override
    public BreachActivityResponseDto getEmailBreachActivity(int days) {
        int windowDays = ALLOWED_ACTIVITY_DAYS.contains(days) ? days : DEFAULT_ACTIVITY_DAYS;
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate fromDate = today.minusDays((long) windowDays - 1);
        Instant fromInstant = fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        TreeMap<LocalDate, BreachActivityBucketDto> bucketsByDate = new TreeMap<>();
        for (int i = 0; i < windowDays; i++) {
            LocalDate day = fromDate.plusDays(i);
            bucketsByDate.put(day, BreachActivityBucketDto.builder().date(day).build());
        }

        List<InsecureWebBreachFinding> findings =
                findingRepository.findEmailFindingsBetween(clientId, fromInstant, toInstant);

        long totalCritical = 0;
        long totalHigh = 0;
        long totalMedium = 0;
        long totalLow = 0;

        for (InsecureWebBreachFinding finding : findings) {
            Instant ts = finding.getTimestamp();
            if (ts == null) {
                continue;
            }
            LocalDate day = ts.atZone(ZoneOffset.UTC).toLocalDate();
            BreachActivityBucketDto bucket = bucketsByDate.get(day);
            if (bucket == null) {
                continue;
            }
            BreachSeverity severity = severityClassifier.classify(finding);
            switch (severity) {
                case CRITICAL:
                    bucket.setCritical(bucket.getCritical() + 1);
                    totalCritical++;
                    break;
                case HIGH:
                    bucket.setHigh(bucket.getHigh() + 1);
                    totalHigh++;
                    break;
                case MEDIUM:
                    bucket.setMedium(bucket.getMedium() + 1);
                    totalMedium++;
                    break;
                case LOW:
                default:
                    bucket.setLow(bucket.getLow() + 1);
                    totalLow++;
                    break;
            }
            bucket.setTotal(bucket.getTotal() + 1);
        }

        return BreachActivityResponseDto.builder()
                .days(windowDays)
                .from(fromDate)
                .to(today)
                .buckets(new ArrayList<>(bucketsByDate.values()))
                .totalCritical(totalCritical)
                .totalHigh(totalHigh)
                .totalMedium(totalMedium)
                .totalLow(totalLow)
                .total(totalCritical + totalHigh + totalMedium + totalLow)
                .build();
    }

    @Override
    public InsecureWebFindingsSummaryDto getFindingsSummary() {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        long breachCount = findingRepository.countEmailFindingsByClientId(clientId);
        long domainCount = configRepository.findByClientId(clientId)
                .map(BreachDetectionConfig::getMonitoredDomains)
                .map(domains -> domains.stream()
                        .filter(StringUtils::hasText)
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .collect(Collectors.toCollection(HashSet::new)))
                .map(Set::size)
                .orElse(0);
        return InsecureWebFindingsSummaryDto.builder()
                .domainCount(domainCount)
                .breachCount(breachCount)
                .build();
    }

    private DateRange resolveDateRange(String fromDate, String toDate) {
        boolean hasFrom = StringUtils.hasText(fromDate);
        boolean hasTo = StringUtils.hasText(toDate);
        if (!hasFrom && !hasTo) {
            return DateRange.inactive();
        }
        if (!hasFrom || !hasTo) {
            throw new IllegalArgumentException("Both fromDate and toDate are required in yyyy-MM-dd format");
        }
        try {
            LocalDate from = LocalDate.parse(fromDate.trim());
            LocalDate to = LocalDate.parse(toDate.trim());
            if (to.isBefore(from)) {
                throw new IllegalArgumentException("toDate must be greater than or equal to fromDate");
            }
            Instant fromInclusive = from.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant toExclusive = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            return new DateRange(fromInclusive, toExclusive);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd for fromDate and toDate");
        }
    }

    @Override
    public AllResponseDto<List<InsecureWebBreachFindingDto>> searchExternalBreaches(InsecureWebBreachSearchRequest request) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        if (request == null) {
            request = InsecureWebBreachSearchRequest.builder().build();
        }
        InsecureWebOrganization organization = organizationRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException(
                        "InsecureWeb organization not yet provisioned for this client. Trigger a sync first."));

        InsecureWebClient.PagedBreachesResponse response =
                insecureWebClient.searchOrganizationBreaches(organization.getInsecureWebOrganizationId(), request);

        List<InsecureWebBreachFindingDto> items = response.getItems().stream()
                .map(item -> mapItemToTransientDto(organization.getInsecureWebOrganizationId(), item))
                .filter(Objects::nonNull)
                .toList();

        return new AllResponseDto<>(
                response.getPage(),
                response.getSize(),
                response.getTotalElements(),
                items
        );
    }

    private InsecureWebBreachFindingDto mapItemToTransientDto(long organizationId, InsecureWebClient.OrganizationBreachItem item) {
        if (item == null) {
            return null;
        }
        BreachSeverity severity = severityClassifier.classify(InsecureWebBreachFinding.builder()
                .password(item.getPassword())
                .password(item.getHashedPassword())
                .phone(item.getPhone())
                .compromisedData(item.getCompromisedData())
                .build());
        return InsecureWebBreachFindingDto.builder()
                .externalFindingId(item.getId())
                .timestamp(item.getTimestamp())
                .domain(item.getDomain())
                .email(item.getEmail())
                .ipAddress(item.getIpAddress())
                .username(item.getUsername())
                .password(item.getHashedPassword())
                .phone(item.getPhone())
                .databaseName(item.getDatabaseName())
                .foundIn(item.getFoundIn())
                .source(item.getSource())
                .leakName(item.getLeakName())
                .breachDescription(item.getBreachDescription())
                .compromisedData(item.getCompromisedData())
                .breachSeverity(severity)
                .victimDomain(item.getVictimDomain())
                .breachStatus(item.getBreachStatus() == null ? InsecureWebBreachStatus.UNKNOWN : item.getBreachStatus())
                .employee(item.getEmployee())
                .echoesCount(item.getEchoesCount())
                .build();
    }

    @Override
    public InsecureWebBreachFindingDto getFindingById(String id) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        InsecureWebBreachFinding finding = findingRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new RuntimeException("Breach finding not found"));
        return toFindingDto(finding);
    }

    @Override
    public AllResponseDto<List<String>> getSources(int offset, int pageSize, String sortBy, String sortDirection) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        int safeOffset = Math.max(offset, 0);
        int safePageSize = Math.max(pageSize, 1);
        int skip = safeOffset * safePageSize;
        String safeSortBy = "source";
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        if (!"source".equalsIgnoreCase(sortBy)) {
            safeSortBy = "source";
        }

        Criteria criteria = Criteria.where("clientId").is(clientId)
                .and("source").ne(null).ne("");

        Aggregation totalAggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("source"),
                Aggregation.count().as("total")
        );
        long totalSources = Optional.ofNullable(
                        mongoTemplate.aggregate(totalAggregation, "insecureweb_breach_findings", SourceTotal.class)
                                .getUniqueMappedResult())
                .map(SourceTotal::getTotal)
                .orElse(0L);

        Aggregation pageAggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("source"),
                Aggregation.project().and("_id").as("source"),
                Aggregation.sort(Sort.by(direction, safeSortBy)),
                Aggregation.skip(skip),
                Aggregation.limit(safePageSize)
        );

        List<String> sources = mongoTemplate.aggregate(pageAggregation, "insecureweb_breach_findings", SourceProjection.class)
                .getMappedResults().stream()
                .map(SourceProjection::getSource)
                .filter(StringUtils::hasText)
                .toList();

        return new AllResponseDto<>(safeOffset, safePageSize, totalSources, sources);
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

    private InsecureWebBreachFindingDto toFindingDto(InsecureWebBreachFinding finding) {
        BreachSeverity severity = severityClassifier.classify(finding);
        return InsecureWebBreachFindingDto.builder()
                .id(finding.getId())
                .externalFindingId(finding.getExternalFindingId())
                .timestamp(finding.getTimestamp())
                .domain(finding.getDomain())
                .email(finding.getEmail())
                .ipAddress(finding.getIpAddress())
                .username(finding.getUsername())
                .password(finding.getPassword())
                .phone(finding.getPhone())
                .databaseName(finding.getDatabaseName())
                .foundIn(finding.getFoundIn())
                .source(finding.getSource())
                .leakName(finding.getLeakName())
                .breachDescription(finding.getBreachDescription())
                .compromisedData(finding.getCompromisedData())
                .breachSeverity(severity)
                .victimDomain(finding.getVictimDomain())
                .breachStatus(finding.getBreachStatus())
                .employee(finding.getEmployee())
                .echoesCount(finding.getEchoesCount())
                .firstSeenAt(finding.getFirstSeenAt())
                .lastSeenAt(finding.getLastSeenAt())
                .createdAt(finding.getCreatedAt())
                .updatedAt(finding.getUpdatedAt())
                .build();
    }

    private static class UpsertOutcome {
        private final boolean inserted;
        private final boolean updated;

        private UpsertOutcome(boolean inserted, boolean updated) {
            this.inserted = inserted;
            this.updated = updated;
        }

        private static UpsertOutcome inserted() {
            return new UpsertOutcome(true, false);
        }

        private static UpsertOutcome updated() {
            return new UpsertOutcome(false, true);
        }

        private static UpsertOutcome none() {
            return new UpsertOutcome(false, false);
        }
    }

    @lombok.Data
    private static class SourceProjection {
        private String source;
    }

    @lombok.Data
    private static class SourceTotal {
        private long total;
    }

    private record DateRange(Instant fromInclusive, Instant toExclusive) {
        static DateRange inactive() {
            return new DateRange(null, null);
        }

        boolean isActive() {
            return fromInclusive != null && toExclusive != null;
        }
    }
}
