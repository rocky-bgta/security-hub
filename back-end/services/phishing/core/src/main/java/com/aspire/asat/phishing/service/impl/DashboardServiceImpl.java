package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.CohortRiskTrendUi;
import com.aspire.asat.phishing.dto.enums.DashboardLookbackDays;
import com.aspire.asat.phishing.dto.enums.HumanRiskTierToken;
import com.aspire.asat.phishing.dto.enums.PhishProneBucketTier;
import com.aspire.asat.phishing.dto.enums.ReportRateTrendUi;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.dto.enums.StatsPeriod;
import com.aspire.asat.phishing.dto.response.AdminDashboardUiDto;
import com.aspire.asat.phishing.dto.response.AssetInventoryCountsDto;
import com.aspire.asat.phishing.dto.response.BreachSummaryDto;
import com.aspire.asat.phishing.dto.response.CampaignPerformanceDto;
import com.aspire.asat.phishing.dto.response.ClientDashboardAssetCountsDto;
import com.aspire.asat.phishing.dto.response.DashboardKpiDto;
import com.aspire.asat.phishing.dto.response.DashboardOverviewDto;
import com.aspire.asat.phishing.dto.response.DashboardTrendsDto;
import com.aspire.asat.phishing.dto.response.EmailStatsDto;
import com.aspire.asat.phishing.dto.response.HumanRiskScoreUiDto;
import com.aspire.asat.phishing.dto.response.PhishingPerformanceDto;
import com.aspire.asat.phishing.dto.response.PhishProneTierUiDto;
import com.aspire.asat.phishing.dto.response.ReportRateUiDto;
import com.aspire.asat.phishing.dto.response.TopRiskUserDto;
import com.aspire.asat.phishing.dto.response.TrendDataPointDto;
import com.aspire.asat.phishing.dto.response.UserRiskDistributionDto;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.model.AdminDashboardAggregate;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.model.CohortMetrics;
import com.aspire.asat.phishing.model.DashboardStats;
import com.aspire.asat.phishing.model.EmailMetrics;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.model.UserRiskMetrics;
import com.aspire.asat.phishing.repository.AdminDashboardAggregateRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.DashboardStatsRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.repository.VishingScenarioRepository;
import com.aspire.asat.phishing.repository.custom.ClientWindowActivityTotals;
import com.aspire.asat.phishing.repository.custom.DailyEmailMetricsAggregation;
import com.aspire.asat.phishing.repository.custom.RecipientWindowActivityMetrics;
import com.aspire.asat.phishing.repository.custom.UserRiskProfileEmailTotals;
import com.aspire.asat.phishing.service.CampaignEmailCounters;
import com.aspire.asat.phishing.service.DashboardAggregationService;
import com.aspire.asat.phishing.service.DashboardCampaignRollupService;
import com.aspire.asat.phishing.service.DashboardService;
import com.aspire.asat.phishing.service.support.CampaignPerformanceSupport;
import com.aspire.asat.phishing.service.support.DashboardChannelScope;
import com.aspire.asat.phishing.utils.CohortMetricsCalculator;
import com.aspire.asat.phishing.utils.CohortMetricsCalculator.CohortMetricsResult;
import com.aspire.asat.phishing.utils.CohortMetricsCalculator.UserActivityCounters;
import com.aspire.asat.phishing.utils.RiskScoreUtils;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for dashboard operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final UserCurrentContextService userCurrentContextService;
    private final CampaignRepository campaignRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final LandingPageRepository landingPageRepository;
    private final SenderProfileRepository senderProfileRepository;
    private final UserRiskProfileRepository userRiskProfileRepository;
    private final EmailActivityRepository emailActivityRepository;
    private final AdminDashboardAggregateRepository adminDashboardAggregateRepository;
    private final DashboardStatsRepository dashboardStatsRepository;
    private final DashboardAggregationService dashboardAggregationService;
    private final DashboardCampaignRollupService campaignRollupService;
    private final RegistrationServiceClient registrationServiceClient;
    private final VishingScenarioRepository vishingScenarioRepository;

    @Override
    public DashboardOverviewDto getOverview() {
        return getOverview(CampaignChannel.EMAIL);
    }

    @Override
    public DashboardOverviewDto getOverview(CampaignChannel channel) {
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        log.info("Getting dashboard overview for client: {}, channel: {}", clientId, effectiveChannel);

        // Campaign metrics
        long totalCampaigns = campaignRepository.countByClientIdAndChannel(clientId, effectiveChannel, null);
        long activeCampaigns = campaignRepository.countByClientIdAndChannel(clientId, effectiveChannel, CampaignStatus.RUNNING);
        long completedCampaigns = campaignRepository.countByClientIdAndChannel(clientId, effectiveChannel, CampaignStatus.COMPLETED);
        long draftCampaigns = campaignRepository.countByClientIdAndChannel(clientId, effectiveChannel, CampaignStatus.DRAFT);

        // Calculate email metrics from campaigns
        EmailStatsDto emailStats = getEmailStats(effectiveChannel);

        // User risk metrics
        UserRiskDistributionDto riskDistribution = getUserRiskDistribution(effectiveChannel);
        AdminDashboardAggregate latestAggregate = getLatestAdminDashboardAggregate(clientId).orElse(null);
        CohortMetrics cohortMetrics = latestAggregate != null && latestAggregate.getCohortMetrics() != null
                ? latestAggregate.getCohortMetrics()
                : new CohortMetrics();
        List<TopRiskUserDto> topRiskUsers = latestAggregate != null && latestAggregate.getTopRiskUsers() != null
                ? latestAggregate.getTopRiskUsers().stream()
                .map(user -> TopRiskUserDto.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .phishingRiskScore(user.getPhishingRiskScore())
                        .riskLevel(user.getRiskLevel())
                        .build())
                .toList()
                : new ArrayList<>();

        DashboardTrendsDto trends = getDashboardTrends(30, effectiveChannel);

        return DashboardOverviewDto.builder()
                .totalCampaigns((int) totalCampaigns)
                .activeCampaigns((int) activeCampaigns)
                .completedCampaigns((int) completedCampaigns)
                .draftCampaigns((int) draftCampaigns)
                .totalEmailsSent(emailStats.getTotalEmailsSent())
                .avgOpenRate(emailStats.getOpenRate())
                .avgClickRate(emailStats.getClickRate())
                .phishPronePercentage(emailStats.getPhishPronePercentage())
                .totalUsers(riskDistribution.getTotalUsers())
                .highRiskUsers(riskDistribution.getHighRiskCount())
                .criticalRiskUsers(riskDistribution.getCriticalRiskCount())
                .repeatOffenders(riskDistribution.getRepeatOffenders())
                .averagePhishingRiskScore(cohortMetrics.getAveragePhishingRiskScore())
                .cohortRiskLevel(cohortMetrics.getCohortRiskLevel() != null ? cohortMetrics.getCohortRiskLevel().name() : RiskLevel.LOW.name())
                .phishProneUsers(String.format("%d Critical | %d High",
                        cohortMetrics.getPhishProneCriticalCount(),
                        cohortMetrics.getPhishProneHighCount()))
                .informationSubmits(cohortMetrics.getInformationSubmitCount())
                .reportRateWithTrend(String.format("%.1f%% %s",
                        cohortMetrics.getReportRatePercent(),
                        cohortMetrics.getReportRateDirection()))
                .riskTrend(cohortMetrics.getRiskTrendStatus() + " " + cohortMetrics.getRiskTrendDirection())
                .breachesDetected(0) // TODO: Integrate with breach detection when Task-08 is complete
                .affectedUsers(0)
                .openRateTrend(trends.getOpenRateTrend())
                .clickRateTrend(trends.getClickRateTrend())
                .submissionRateTrend(trends.getSubmissionRateTrend())
                .reportRateTrend(trends.getReportRateTrend())
                .topRiskUsers(topRiskUsers)
                .build();
    }

    @Override
    public AdminDashboardUiDto getAdminUiData(int days) {
        return getAdminUiData(days, CampaignChannel.EMAIL);
    }

    @Override
    public AdminDashboardUiDto getAdminUiData(int days, CampaignChannel channel) {
        int windowDays;
        try {
            windowDays = DashboardLookbackDays.resolve(days).getDays();
        } catch (IllegalArgumentException ex) {
            throw new PhishingValidationException(ex.getMessage());
        }

        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        List<String> campaignIds = campaignRepository.findIdsByClientIdAndChannel(clientId, effectiveChannel);
        Instant now = Instant.now();
        Instant currentStart = now.minus(windowDays, ChronoUnit.DAYS);
        Instant previousStart = now.minus(windowDays * 2L, ChronoUnit.DAYS);
        DashboardChannelScope.ActivityMapping mapping = DashboardChannelScope.activityMapping(effectiveChannel);

        List<RecipientWindowActivityMetrics> currentRows =
                emailActivityRepository.aggregateRecipientMetricsForWindow(
                        clientId, currentStart, now, campaignIds, mapping);
        if (currentRows.isEmpty()) {
            return emptyAdminDashboardUi();
        }

        List<RecipientWindowActivityMetrics> previousRows =
                emailActivityRepository.aggregateRecipientMetricsForWindow(
                        clientId, previousStart, currentStart, campaignIds, mapping);

        List<UserActivityCounters> previousCounters = toActivityCounters(clientId, previousRows);
        CohortMetricsResult previousResult = CohortMetricsCalculator.compute(previousCounters, 0.0, 0.0);

        List<UserActivityCounters> currentCounters = toActivityCounters(clientId, currentRows);
        CohortMetricsResult cohortResult = CohortMetricsCalculator.compute(
                currentCounters,
                previousResult.averagePhishingRiskScore(),
                previousResult.reportRatePercent());

        return toAdminDashboardUi(cohortResult);
    }

    private List<UserActivityCounters> toActivityCounters(
            String clientId,
            List<RecipientWindowActivityMetrics> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }

        Set<String> emails = rows.stream()
                .map(RecipientWindowActivityMetrics::recipientEmail)
                .filter(email -> email != null && !email.isBlank())
                .collect(Collectors.toSet());
        Map<String, String> emailToUserId = userRiskProfileRepository.findByClientIdAndEmailIn(clientId, emails).stream()
                .filter(profile -> profile.getEmail() != null && !profile.getEmail().isBlank())
                .collect(Collectors.toMap(
                        UserRiskProfile::getEmail,
                        UserRiskProfile::getUserId,
                        (left, right) -> left));

        return rows.stream()
                .map(row -> CohortMetricsCalculator.fromWindowMetrics(
                        row,
                        emailToUserId.get(row.recipientEmail())))
                .filter(counter -> counter.userId() != null && !counter.userId().isBlank())
                .toList();
    }

    private AdminDashboardUiDto toAdminDashboardUi(CohortMetricsResult cohortResult) {
        double avg = cohortResult.averagePhishingRiskScore();
        RiskLevel cohortLevel = cohortResult.cohortRiskLevel() != null
                ? cohortResult.cohortRiskLevel()
                : RiskScoreUtils.fromScore(avg);

        int roundedScore = (int) Math.round(avg);
        roundedScore = Math.max(0, Math.min(100, roundedScore));

        HumanRiskScoreUiDto humanRiskScore = HumanRiskScoreUiDto.builder()
                .score(roundedScore)
                .tier(HumanRiskTierToken.fromRiskLevel(cohortLevel))
                .build();

        List<PhishProneTierUiDto> phishProneTiers = List.of(
                PhishProneTierUiDto.builder()
                        .tier(PhishProneBucketTier.CRITICAL)
                        .count(cohortResult.phishProneCriticalCount())
                        .build(),
                PhishProneTierUiDto.builder()
                        .tier(PhishProneBucketTier.HIGH)
                        .count(cohortResult.phishProneHighCount())
                        .build()
        );

        ReportRateUiDto reportRate = ReportRateUiDto.builder()
                .current(String.format(Locale.US, "%.1f%%", cohortResult.reportRatePercent()))
                .trend(ReportRateTrendUi.fromStoredDirection(cohortResult.reportRateDirection()))
                .build();

        CohortRiskTrendUi riskTrend = CohortRiskTrendUi.fromAggregateStatus(cohortResult.riskTrendStatus());

        List<TopRiskUserDto> topRiskUsers = cohortResult.topRiskUsers().stream()
                .map(user -> TopRiskUserDto.builder()
                        .userId(user.getUserId())
                        .email(user.getEmail())
                        .phishingRiskScore(user.getPhishingRiskScore())
                        .riskLevel(user.getRiskLevel())
                        .build())
                .toList();

        return AdminDashboardUiDto.builder()
                .humanRiskScore(humanRiskScore)
                .phishProneUsers(phishProneTiers)
                .informationSubmits(cohortResult.informationSubmitCount())
                .reportRate(reportRate)
                .riskTrend(riskTrend)
                .topRiskUsers(topRiskUsers)
                .build();
    }

    private static AdminDashboardUiDto emptyAdminDashboardUi() {
        return AdminDashboardUiDto.builder()
                .humanRiskScore(HumanRiskScoreUiDto.builder().score(0).tier(HumanRiskTierToken.LOW_RISK).build())
                .phishProneUsers(List.of(
                        PhishProneTierUiDto.builder().tier(PhishProneBucketTier.CRITICAL).count(0).build(),
                        PhishProneTierUiDto.builder().tier(PhishProneBucketTier.HIGH).count(0).build()))
                .informationSubmits(0)
                .reportRate(ReportRateUiDto.builder()
                        .current(String.format(Locale.US, "%.1f%%", 0.0))
                        .trend(ReportRateTrendUi.STABLE)
                        .build())
                .riskTrend(CohortRiskTrendUi.STABLE)
                .build();
    }

    @Override
    public DashboardKpiDto getKpiMetrics(int days) {
        return getKpiMetrics(days, CampaignChannel.EMAIL);
    }

    @Override
    public DashboardKpiDto getKpiMetrics(int days, CampaignChannel channel) {
        int windowDays;
        try {
            windowDays = DashboardLookbackDays.resolve(days).getDays();
        } catch (IllegalArgumentException ex) {
            throw new PhishingValidationException(ex.getMessage());
        }

        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        log.info("Getting KPI metrics for client: {}, windowDays={}, channel={}", clientId, windowDays, effectiveChannel);

        Instant now = Instant.now();
        Instant start = now.minus(windowDays, ChronoUnit.DAYS);
        List<String> campaignIds = campaignRepository.findIdsByClientIdAndChannel(clientId, effectiveChannel);

        ClientWindowActivityTotals activity =
                emailActivityRepository.aggregateClientActivityTotalsForWindow(
                        clientId, start, now, campaignIds, DashboardChannelScope.activityMapping(effectiveChannel));

        int attacks = activity.emailsSent();
        int hacks = activity.dataSubmitted();
        int reports = activity.emailsReported();
        int campaigns = (int) campaignRepository.countLaunchedBetweenByChannel(
                clientId, start, now, effectiveChannel);
        int templates = countKpiTemplates(clientId, effectiveChannel);
        int landingPages = countKpiLandingPages(clientId, effectiveChannel);
        int groups = 0;

        double compromiseRate = attacks > 0 ? (double) hacks / attacks * 100 : 0;
        double reportRate = attacks > 0 ? (double) reports / attacks * 100 : 0;

        return DashboardKpiDto.builder()
                .attacks(attacks)
                .hacks(hacks)
                .reports(reports)
                .campaigns(campaigns)
                .templates(templates)
                .groups(groups)
                .landingPages(landingPages)
                .compromiseRate(Math.round(compromiseRate * 10) / 10.0)
                .reportRate(Math.round(reportRate * 10) / 10.0)
                .build();
    }

    private int countKpiTemplates(String clientId, CampaignChannel channel) {
        return switch (channel) {
            case SMS -> (int) emailTemplateRepository.countSmsTemplatesByClientIdOrIsGlobal(clientId);
            case VOICE -> (int) vishingScenarioRepository.countByClientIdOrIsGlobal(clientId);
            default -> (int) emailTemplateRepository.countEmailTemplatesByClientIdOrIsGlobal(clientId);
        };
    }

    private int countKpiLandingPages(String clientId, CampaignChannel channel) {
        if (channel == CampaignChannel.VOICE) {
            return 0;
        }
        return (int) landingPageRepository.countByClientIdOrIsGlobal(clientId);
    }

    @Override
    public ClientDashboardAssetCountsDto getAssetCounts() {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        log.info("Getting client dashboard asset counts for client: {}", clientId);

        return ClientDashboardAssetCountsDto.builder()
                .numberOfEmailTemplates((int) emailTemplateRepository.countByClientIdOrIsGlobal(clientId))
                .numberOfLandingPages((int) landingPageRepository.countByClientIdOrIsGlobal(clientId))
                .numberOfSenderProfiles((int) senderProfileRepository.countByClientIdOrIsGlobal(clientId))
                .build();
    }

    @Override
    public AssetInventoryCountsDto getAssetInventoryCounts() {
        log.info("Fetching asset inventory counts");
        return AssetInventoryCountsDto.builder()
                .campaignPresets(campaignRepository.count())
                .emailTemplates(emailTemplateRepository.count())
                .sendingProfiles(senderProfileRepository.count())
                .landingPages(landingPageRepository.count())
                .build();
    }

    @Override
    public PhishingPerformanceDto getPhishingPerformance(String mspId) {
        String resolvedMspId = resolveMspId(mspId);
        var totals = resolvedMspId != null
                ? aggregateForMsp(resolvedMspId)
                : userRiskProfileRepository.aggregateEmailTotals();

        log.info("Fetching phishing performance resolvedMspId={}", resolvedMspId);

        long received = totals.emailsReceived();
        long reported = totals.emailsReported();
        long ignored = Math.max(0L, received - totals.emailsOpened());
        long clicked = totals.linksClicked();

        return PhishingPerformanceDto.builder()
                .emailsReceived(received)
                .emailsReported(reported)
                .emailsIgnored(ignored)
                .linksClicked(clicked)
                .reportedPercentage(toPercentage(reported, received))
                .ignoredOrNotOpenedPercentage(toPercentage(ignored, received))
                .clickedPercentage(toPercentage(clicked, received))
                .build();
    }

    private UserRiskProfileEmailTotals aggregateForMsp(String mspId) {
        List<String> clientAdminIds = registrationServiceClient.getClientAdminIdsByMspId(mspId);
        log.info("MSP phishing performance mspId={} clientAdminCount={}", mspId, clientAdminIds.size());
        return userRiskProfileRepository.aggregateEmailTotalsForClientIds(clientAdminIds);
    }

    /**
     * Explicit mspId wins. If omitted and caller is MSP, use context userId as mspId.
     * Otherwise return null for platform-wide totals.
     */
    private String resolveMspId(String requestMspId) {
        if (requestMspId != null && !requestMspId.isBlank()) {
            return requestMspId.trim();
        }

        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        if (context == null) {
            return null;
        }

        if (isMspUser(context)
                && context.getUserId() != null
                && !context.getUserId().isBlank()) {
            return context.getUserId().trim();
        }

        return null;
    }

    private boolean isMspUser(CurrentUserContext context) {
        try {
            return UserType.MSP.equals(UserType.fromString(context.getUserType()));
        } catch (IllegalArgumentException e) {
            return UserType.MSP.name().equalsIgnoreCase(context.getUserType());
        }
    }

    private static final Set<CampaignStatus> REPORTABLE_CAMPAIGN_STATUSES = Set.of(
            CampaignStatus.RUNNING,
            CampaignStatus.COMPLETED,
            CampaignStatus.EXPIRED
    );

    @Override
    public List<CampaignPerformanceDto> getCampaignPerformance(int limit, CampaignType campaignType) {
        return getCampaignPerformance(limit, campaignType, CampaignChannel.EMAIL);
    }

    @Override
    public List<CampaignPerformanceDto> getCampaignPerformance(
            int limit, CampaignType campaignType, CampaignChannel channel) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        log.info("Getting campaign performance for client: {}, campaignType={}, channel={}",
                clientId, campaignType, effectiveChannel);

        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Campaign> campaigns = campaignRepository.findWithStatuses(
                clientId,
                null,
                REPORTABLE_CAMPAIGN_STATUSES,
                effectiveChannel,
                campaignType,
                null,
                null,
                false,
                pageRequest).getContent();

        return campaigns.stream()
                .map(CampaignPerformanceSupport::toPerformanceDto)
                .toList();
    }

    @Override
    public EmailStatsDto getEmailStats() {
        return getEmailStats(CampaignChannel.EMAIL);
    }

    @Override
    public EmailStatsDto getEmailStats(CampaignChannel channel) {
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        CampaignEmailCounters counters = campaignRollupService.rollupEmailCounters(clientId, effectiveChannel);

        int totalRecipients = counters.totalRecipients();
        int totalSent = counters.totalEmailsSent();
        int delivered = counters.emailsDelivered();
        int bounced = counters.emailsBounced();
        int opened = counters.emailsOpened();
        int clicked = counters.linksClicked();
        int attachments = counters.attachmentsOpened();
        int submitted = counters.dataSubmitted();
        int reported = counters.emailsReported();

        double deliveryRate = totalSent > 0 ? (double) delivered / totalSent * 100 : 0;
        double openRate = totalRecipients > 0 ? (double) opened / totalRecipients * 100 : 0;
        double clickRate = totalRecipients > 0 ? (double) clicked / totalRecipients * 100 : 0;
        double compromiseRate = clicked > 0 ? (double) submitted / clicked * 100 : 0;
        double reportRate = totalRecipients > 0 ? (double) reported / totalRecipients * 100 : 0;
        double phishProne = totalSent > 0 ? (double) clicked / totalSent * 100 : 0;

        return EmailStatsDto.builder()
                .totalRecipients(totalRecipients)
                .totalEmailsSent(totalSent)
                .emailsDelivered(delivered)
                .emailsBounced(bounced)
                .emailsOpened(opened)
                .linksClicked(clicked)
                .attachmentsOpened(attachments)
                .dataSubmitted(submitted)
                .emailsReported(reported)
                .deliveryRate(Math.round(deliveryRate * 10) / 10.0)
                .openRate(Math.round(openRate * 10) / 10.0)
                .clickRate(Math.round(clickRate * 10) / 10.0)
                .compromiseRate(Math.round(compromiseRate * 10) / 10.0)
                .reportRate(Math.round(reportRate * 10) / 10.0)
                .phishPronePercentage(Math.round(phishProne * 10) / 10.0)
                .build();
    }

    @Override
    public UserRiskDistributionDto getUserRiskDistribution() {
        return getUserRiskDistribution(CampaignChannel.EMAIL);
    }

    @Override
    public UserRiskDistributionDto getUserRiskDistribution(CampaignChannel channel) {
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        Instant now = Instant.now();
        Instant start = now.minus(30, ChronoUnit.DAYS);
        List<String> campaignIds = campaignRepository.findIdsByClientIdAndChannel(clientId, effectiveChannel);
        List<RecipientWindowActivityMetrics> rows =
                emailActivityRepository.aggregateRecipientMetricsForWindow(
                        clientId, start, now, campaignIds, DashboardChannelScope.activityMapping(effectiveChannel));
        List<UserActivityCounters> counters = toActivityCounters(clientId, rows);

        int total = counters.size();
        int low = 0;
        int medium = 0;
        int high = 0;
        int critical = 0;
        int repeatOffenders = 0;
        int compromised = 0;
        for (UserActivityCounters user : counters) {
            RiskLevel level = RiskScoreUtils.fromScore(user.phishingRiskScore());
            switch (level) {
                case LOW -> low++;
                case MEDIUM -> medium++;
                case HIGH -> high++;
                case CRITICAL -> critical++;
            }
            if (user.clicked() >= 3) {
                repeatOffenders++;
            }
            if (user.submits() > 0) {
                compromised++;
            }
        }

        return UserRiskDistributionDto.builder()
                .totalUsers(total)
                .lowRiskCount(low)
                .mediumRiskCount(medium)
                .highRiskCount(high)
                .criticalRiskCount(critical)
                .lowRiskPercentage(total > 0 ? Math.round((double) low / total * 1000) / 10.0 : 0)
                .mediumRiskPercentage(total > 0 ? Math.round((double) medium / total * 1000) / 10.0 : 0)
                .highRiskPercentage(total > 0 ? Math.round((double) high / total * 1000) / 10.0 : 0)
                .criticalRiskPercentage(total > 0 ? Math.round((double) critical / total * 1000) / 10.0 : 0)
                .repeatOffenders(repeatOffenders)
                .compromisedUsers(compromised)
                .build();
    }

    @Override
    public DashboardTrendsDto getDashboardTrends(int days) {
        return getDashboardTrends(days, CampaignChannel.EMAIL);
    }

    @Override
    public DashboardTrendsDto getDashboardTrends(int days, CampaignChannel channel) {
        int windowDays = resolveTrendWindowDays(days);
        return buildDashboardTrendsDto(windowDays, DashboardChannelScope.effective(channel));
    }

    @Override
    public List<TrendDataPointDto> getTrends(String metricType, int days) {
        return getTrends(metricType, days, CampaignChannel.EMAIL);
    }

    @Override
    public List<TrendDataPointDto> getTrends(String metricType, int days, CampaignChannel channel) {
        CampaignChannel effectiveChannel = DashboardChannelScope.effective(channel);
        if ("phishProne".equals(metricType)) {
            return buildSingleMetricTrendSeries(metricType, resolveTrendWindowDays(days), effectiveChannel);
        }
        DashboardTrendsDto all = getDashboardTrends(days, effectiveChannel);
        return switch (metricType) {
            case "openRate" -> all.getOpenRateTrend();
            case "clickRate" -> all.getClickRateTrend();
            case "compromiseRate" -> all.getSubmissionRateTrend();
            case "reportRate" -> all.getReportRateTrend();
            default -> List.of();
        };
    }

    private int resolveTrendWindowDays(int days) {
        try {
            return DashboardLookbackDays.resolve(days).getDays();
        } catch (IllegalArgumentException ex) {
            throw new PhishingValidationException(ex.getMessage());
        }
    }

    private DashboardTrendsDto buildDashboardTrendsDto(int days, CampaignChannel channel) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        List<TrendDataPointDto> openRateTrend = new ArrayList<>();
        List<TrendDataPointDto> clickRateTrend = new ArrayList<>();
        List<TrendDataPointDto> submissionRateTrend = new ArrayList<>();
        List<TrendDataPointDto> reportRateTrend = new ArrayList<>();

        if (isGlobalDashboardViewer()) {
            List<DailyEmailMetricsAggregation> rows = dashboardStatsRepository
                    .aggregateDailyEmailMetricsForAllNonBlankClients(startDate, channel);
            for (DailyEmailMetricsAggregation row : rows) {
                appendBundledTrendPoints(row.date(), row.emailMetrics(),
                        openRateTrend, clickRateTrend, submissionRateTrend, reportRateTrend);
            }
        } else {
            String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
            List<DashboardStats> dailyStats = dashboardStatsRepository
                    .findDailyStatsForLastNDays(clientId, startDate, channel);
            for (DashboardStats stats : dailyStats) {
                appendBundledTrendPoints(stats.getDate(), trendMetrics(stats, channel),
                        openRateTrend, clickRateTrend, submissionRateTrend, reportRateTrend);
            }
        }

        return DashboardTrendsDto.builder()
                .openRateTrend(openRateTrend)
                .clickRateTrend(clickRateTrend)
                .submissionRateTrend(submissionRateTrend)
                .reportRateTrend(reportRateTrend)
                .build();
    }

    private List<TrendDataPointDto> buildSingleMetricTrendSeries(
            String metricType, int days, CampaignChannel channel) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        List<TrendDataPointDto> trends = new ArrayList<>();

        if (isGlobalDashboardViewer()) {
            List<DailyEmailMetricsAggregation> rows = dashboardStatsRepository
                    .aggregateDailyEmailMetricsForAllNonBlankClients(startDate, channel);
            for (DailyEmailMetricsAggregation row : rows) {
                trends.add(toTrendPoint(row.date(), metricFromEmailMetrics(row.emailMetrics(), metricType)));
            }
            return trends;
        }

        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        List<DashboardStats> dailyStats = dashboardStatsRepository
                .findDailyStatsForLastNDays(clientId, startDate, channel);
        for (DashboardStats stats : dailyStats) {
            trends.add(toTrendPoint(stats.getDate(), metricFromEmailMetrics(trendMetrics(stats, channel), metricType)));
        }
        return trends;
    }

    private static void appendBundledTrendPoints(
            LocalDate date,
            EmailMetrics metrics,
            List<TrendDataPointDto> openRateTrend,
            List<TrendDataPointDto> clickRateTrend,
            List<TrendDataPointDto> submissionRateTrend,
            List<TrendDataPointDto> reportRateTrend) {
        openRateTrend.add(toTrendPoint(date, metricFromEmailMetrics(metrics, "openRate")));
        clickRateTrend.add(toTrendPoint(date, metricFromEmailMetrics(metrics, "clickRate")));
        submissionRateTrend.add(toTrendPoint(date, metricFromEmailMetrics(metrics, "compromiseRate")));
        reportRateTrend.add(toTrendPoint(date, metricFromEmailMetrics(metrics, "reportRate")));
    }

    private static TrendDataPointDto toTrendPoint(LocalDate date, double value) {
        return TrendDataPointDto.builder()
                .date(date)
                .value(value)
                .label(date.toString())
                .build();
    }

    private boolean isGlobalDashboardViewer() {
        try {
            String ut = userCurrentContextService.getCurrentUserContext().getUserType();
            if (ut == null || ut.isBlank()) {
                return false;
            }
            UserType type = UserType.fromString(ut.trim());
            return type == UserType.SYSTEM_USER || type == UserType.ASPIRE_ADMIN || type == UserType.SUPER_ADMIN;
        } catch (Exception e) {
            log.debug("Could not resolve user type for dashboard trends: {}", e.getMessage());
            return false;
        }
    }

    private static EmailMetrics trendMetrics(DashboardStats stats, CampaignChannel channel) {
        if (stats == null) {
            return null;
        }
        return switch (DashboardChannelScope.effective(channel)) {
            case SMS -> stats.getSmsMetrics();
            case VOICE -> stats.getVoiceMetrics();
            default -> stats.getEmailMetrics();
        };
    }

    private static double metricFromEmailMetrics(EmailMetrics metrics, String metricType) {
        if (metrics == null) {
            return 0;
        }
        return switch (metricType) {
            case "openRate" -> metrics.getOpenRate();
            case "clickRate" -> metrics.getClickRate();
            case "compromiseRate" -> metrics.getSubmissionRate();
            case "reportRate" -> metrics.getReportRate();
            case "phishProne" -> metrics.getPhishPronePercentage();
            default -> 0;
        };
    }

    @Override
    public BreachSummaryDto getBreachSummary() {
        return getBreachSummary(CampaignChannel.EMAIL);
    }

    @Override
    public BreachSummaryDto getBreachSummary(CampaignChannel channel) {
        // TODO: Implement when Task-08 Breach Detection is complete
        return BreachSummaryDto.builder()
                .totalBreaches(0)
                .affectedUsers(0)
                .uniqueDomains(0)
                .resolvedBreaches(0)
                .pendingActions(0)
                .breachesThisMonth(0)
                .breachesLastMonth(0)
                .monthOverMonthChange(0)
                .breachesBySource(new ArrayList<>())
                .build();
    }

    @Override
    public void refreshStats() {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        log.info("Refreshing dashboard stats for client: {}", clientId);
        int usersIncluded = dashboardAggregationService.aggregateForClient(clientId);
        log.info("Dashboard stats refreshed for client: {}, usersIncludedInAggregate={}", clientId, usersIncluded);
    }

    private java.util.Optional<AdminDashboardAggregate> getLatestAdminDashboardAggregate(String clientId) {
        return adminDashboardAggregateRepository.findTopByClientIdAndPeriodOrderByDateDesc(clientId, StatsPeriod.DAILY);
    }

    private static double toPercentage(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0.0;
        }
        return Math.round((double) numerator / denominator * 1000.0) / 10.0;
    }
}
