package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.StatsPeriod;
import com.aspire.asat.phishing.model.AdminDashboardAggregate;
import com.aspire.asat.phishing.model.CampaignMetrics;
import com.aspire.asat.phishing.model.CohortMetrics;
import com.aspire.asat.phishing.model.DashboardAggregationAudit;
import com.aspire.asat.phishing.model.EmailMetrics;
import com.aspire.asat.phishing.model.TopRiskUserSnapshot;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.model.UserRiskMetrics;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.repository.AdminDashboardAggregateRepository;
import com.aspire.asat.phishing.repository.DashboardAggregationAuditRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.service.CampaignEmailCounters;
import com.aspire.asat.phishing.service.DashboardAggregationService;
import com.aspire.asat.phishing.service.DashboardCampaignRollupService;
import com.aspire.asat.phishing.service.DashboardStatsWriter;
import com.aspire.asat.phishing.utils.CohortMetricsCalculator;
import com.aspire.asat.phishing.utils.CohortMetricsCalculator.CohortMetricsResult;
import com.aspire.asat.phishing.utils.CohortMetricsCalculator.UserActivityCounters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardAggregationServiceImpl implements DashboardAggregationService {

    private final UserRiskProfileRepository userRiskProfileRepository;
    private final AdminDashboardAggregateRepository adminDashboardAggregateRepository;
    private final DashboardAggregationAuditRepository dashboardAggregationAuditRepository;
    private final DashboardCampaignRollupService campaignRollupService;
    private final DashboardStatsWriter dashboardStatsWriter;
    private final MongoTemplate mongoTemplate;

    @Value("${dashboard.aggregation.rolling-window-days:90}")
    private int rollingWindowDays;

    @Override
    public void aggregateForAllClients() {
        Set<String> clientIds = new LinkedHashSet<>();
        mongoTemplate.query(UserRiskProfile.class)
                .distinct("clientId")
                .as(String.class)
                .all()
                .stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .forEach(clientIds::add);
        mongoTemplate.query(Campaign.class)
                .distinct("clientId")
                .as(String.class)
                .all()
                .stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .forEach(clientIds::add);

        log.info("Admin dashboard aggregation batch starting: distinctClientIds={}", clientIds.size());

        int clientsProcessed = 0;
        int clientsFailed = 0;
        long totalUserProfilesAcrossClients = 0;

        for (String clientId : clientIds) {
            if (clientId == null || clientId.isBlank()) {
                continue;
            }
            try {
                int usersThisClient = aggregateForClient(clientId);
                totalUserProfilesAcrossClients += usersThisClient;
                clientsProcessed++;
            } catch (Exception ex) {
                clientsFailed++;
                log.error("Dashboard aggregation failed for clientId={}", clientId, ex);
            }
        }

        log.info("Admin dashboard aggregation batch finished: clientsProcessed={}, clientsFailed={}, totalUserProfilesSummed={}",
                clientsProcessed, clientsFailed, totalUserProfilesAcrossClients);
    }

    /**
     * @return number of user risk profiles included in this client's aggregate (for batch logging)
     */
    @Override
    public int aggregateForClient(String clientId) {
        Instant now = Instant.now();
        Instant rollingWindowStart = now.minus(rollingWindowDays, ChronoUnit.DAYS);
        LocalDate today = LocalDate.now();

        List<UserRiskProfile> profiles = userRiskProfileRepository.findByClientId(clientId);
        int userCount = profiles.size();
        log.info("Admin dashboard aggregation: clientId={}, userRiskProfilesLoaded={}, rollingWindowDays={}",
                clientId, userCount, rollingWindowDays);

        Optional<DashboardAggregationAudit> previousAudit = dashboardAggregationAuditRepository
                .findTopByClientIdOrderByRunAtDesc(clientId);

        CohortMetricsResult cohortResult = buildCohortMetrics(profiles, previousAudit);
        CohortMetrics cohortMetrics = toCohortMetrics(cohortResult, previousAudit, rollingWindowStart, now);
        List<TopRiskUserSnapshot> topRiskUsers = cohortResult.topRiskUsers();
        UserRiskMetrics userRiskMetrics = buildUserRiskMetrics(profiles);

        AdminDashboardAggregate stats = adminDashboardAggregateRepository
                .findByClientIdAndPeriodAndDate(clientId, StatsPeriod.DAILY, today)
                .orElse(AdminDashboardAggregate.builder()
                        .clientId(clientId)
                        .period(StatsPeriod.DAILY)
                        .date(today)
                        .build());

        stats.setCohortMetrics(cohortMetrics);
        stats.setTopRiskUsers(topRiskUsers);
        stats.setUserRiskMetrics(userRiskMetrics);
        stats.setUpdatedAt(now);
        adminDashboardAggregateRepository.save(stats);

        dashboardAggregationAuditRepository.save(DashboardAggregationAudit.builder()
                .clientId(clientId)
                .rollingWindowStart(rollingWindowStart)
                .rollingWindowEnd(now)
                .currentAvgHrs(cohortMetrics.getCurrentAvgHrs())
                .previousAvgHrs(cohortMetrics.getPreviousAvgHrs())
                .currentReportRatePercent(cohortMetrics.getReportRatePercent())
                .previousReportRatePercent(previousAudit.map(DashboardAggregationAudit::getCurrentReportRatePercent).orElse(0.0))
                .runAt(now)
                .build());

        log.info("Admin dashboard aggregation saved: clientId={}, usersInCohort={}, topRiskRowsPersisted={}, "
                        + "avgPhishingRisk={}, cohortTier={}, informationSubmits={}, reportRatePercent={}",
                clientId,
                cohortMetrics.getTotalUsers(),
                topRiskUsers.size(),
                cohortMetrics.getAveragePhishingRiskScore(),
                cohortMetrics.getCohortRiskLevel(),
                cohortMetrics.getInformationSubmitCount(),
                cohortMetrics.getReportRatePercent());

        for (CampaignChannel channel : CampaignChannel.values()) {
            CampaignEmailCounters emailCounters = campaignRollupService.rollupEmailCounters(clientId, channel);
            EmailMetrics emailMetrics = campaignRollupService.toEmailMetrics(emailCounters);
            CampaignMetrics campaignMetrics = campaignRollupService.buildCampaignMetrics(clientId, channel);
            dashboardStatsWriter.upsertDailyStats(
                    clientId, today, channel, emailMetrics, campaignMetrics, userRiskMetrics);
        }

        return userCount;
    }

    private CohortMetricsResult buildCohortMetrics(List<UserRiskProfile> profiles,
                                                   Optional<DashboardAggregationAudit> previousAudit) {
        double previousAvg = previousAudit.map(DashboardAggregationAudit::getCurrentAvgHrs).orElse(0.0);
        double previousReportRate = previousAudit
                .map(DashboardAggregationAudit::getCurrentReportRatePercent)
                .orElse(0.0);

        List<UserActivityCounters> counters = profiles.stream()
                .map(CohortMetricsCalculator::fromUserRiskProfile)
                .toList();

        return CohortMetricsCalculator.compute(counters, previousAvg, previousReportRate);
    }

    private static CohortMetrics toCohortMetrics(CohortMetricsResult result,
                                                 Optional<DashboardAggregationAudit> previousAudit,
                                                 Instant rollingWindowStart,
                                                 Instant rollingWindowEnd) {
        double previousAvg = previousAudit.map(DashboardAggregationAudit::getCurrentAvgHrs).orElse(0.0);
        return CohortMetrics.builder()
                .totalUsers(result.totalUsers())
                .averagePhishingRiskScore(result.averagePhishingRiskScore())
                .cohortRiskLevel(result.cohortRiskLevel())
                .phishProneCriticalCount(result.phishProneCriticalCount())
                .phishProneHighCount(result.phishProneHighCount())
                .informationSubmitCount(result.informationSubmitCount())
                .reportRatePercent(result.reportRatePercent())
                .reportRateDirection(result.reportRateDirection())
                .riskTrendStatus(result.riskTrendStatus())
                .riskTrendDirection(result.riskTrendDirection())
                .currentAvgHrs(result.averagePhishingRiskScore())
                .previousAvgHrs(round1(previousAvg))
                .rollingWindowStart(rollingWindowStart)
                .rollingWindowEnd(rollingWindowEnd)
                .build();
    }

    private UserRiskMetrics buildUserRiskMetrics(List<UserRiskProfile> profiles) {
        int low = 0;
        int medium = 0;
        int high = 0;
        int critical = 0;
        int repeatOffenders = 0;
        int compromisedUsers = 0;

        for (UserRiskProfile profile : profiles) {
            if (profile.getRiskLevel() != null) {
                switch (profile.getRiskLevel()) {
                    case LOW -> low++;
                    case MEDIUM -> medium++;
                    case HIGH -> high++;
                    case CRITICAL -> critical++;
                }
            }
            if (profile.getLinksClicked() >= 3) {
                repeatOffenders++;
            }
            if (profile.getDataSubmissions() > 0) {
                compromisedUsers++;
            }
        }

        return UserRiskMetrics.builder()
                .totalUsers(profiles.size())
                .lowRiskUsers(low)
                .mediumRiskUsers(medium)
                .highRiskUsers(high)
                .criticalRiskUsers(critical)
                .repeatOffenders(repeatOffenders)
                .compromisedUsers(compromisedUsers)
                .build();
    }

    private static double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
