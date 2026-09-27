package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.response.UserRiskSummaryDto;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.service.AnalyticsService;
import com.aspire.asat.phishing.service.support.RegistrationRiskGroupSyncService;
import com.aspire.asat.phishing.utils.RiskScoreUtils;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for analytics calculations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserCurrentContextService userCurrentContextService;
    private final CampaignRepository campaignRepository;
    private final UserRiskProfileRepository userRiskProfileRepository;
    private final RegistrationRiskGroupSyncService registrationRiskGroupSyncService;

    @Value("${risk-score.training-weight}")
    private Double trainingWeight;

    @Value("${risk-score.phishing-weight}")
    private Double phishingWeight;

    @Override
    public double getPhishPronePercentage() {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        List<Campaign> campaigns = campaignRepository.findByClientId(clientId, 
                PageRequest.of(0, 1000)).getContent();

        int totalSent = 0;
        int totalClicked = 0;

        for (Campaign campaign : campaigns) {
            CampaignStats stats = campaign.getStats();
            if (stats != null) {
                totalSent += stats.getEmailsSent();
                totalClicked += stats.getLinksClicked();
            }
        }

        // BR-02: Phish-prone % = (clicked / sent) * 100
        return totalSent > 0 ? Math.round((double) totalClicked / totalSent * 1000) / 10.0 : 0;
    }

    @Override
    public double getPhishPronePercentageForCampaign(String campaignId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        Campaign campaign = campaignRepository.findByIdAndClientId(campaignId, clientId)
                .orElse(null);

        if (campaign == null || campaign.getStats() == null) {
            return 0;
        }

        CampaignStats stats = campaign.getStats();
        return stats.getEmailsSent() > 0 
                ? Math.round((double) stats.getLinksClicked() / stats.getEmailsSent() * 1000) / 10.0 
                : 0;
    }

    @Override
    public List<UserRiskSummaryDto> getRepeatOffenders(int limit) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        // BR-04: Repeat offender = clicked 3+ times
        List<UserRiskProfile> offenders = userRiskProfileRepository.findRepeatOffenders(clientId);

        return offenders.stream()
                .limit(limit)
                .map(this::toUserRiskSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    public double getDeliveryRate() {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        List<Campaign> campaigns = campaignRepository.findByClientId(clientId, 
                PageRequest.of(0, 1000)).getContent();

        int totalSent = 0;
        int totalDelivered = 0;

        for (Campaign campaign : campaigns) {
            CampaignStats stats = campaign.getStats();
            if (stats != null) {
                totalSent += stats.getEmailsSent();
                totalDelivered += stats.getEmailsDelivered();
            }
        }

        return totalSent > 0 ? Math.round((double) totalDelivered / totalSent * 1000) / 10.0 : 0;
    }

    @Override
    public double getCompromiseRate() {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        List<Campaign> campaigns = campaignRepository.findByClientId(clientId, 
                PageRequest.of(0, 1000)).getContent();

        int totalClicked = 0;
        int totalSubmitted = 0;

        for (Campaign campaign : campaigns) {
            CampaignStats stats = campaign.getStats();
            if (stats != null) {
                totalClicked += stats.getLinksClicked();
                totalSubmitted += stats.getDataSubmitted();
            }
        }

        return totalClicked > 0 ? Math.round((double) totalSubmitted / totalClicked * 1000) / 10.0 : 0;
    }

    @Override
    public double getReportRate() {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        List<Campaign> campaigns = campaignRepository.findByClientId(clientId, 
                PageRequest.of(0, 1000)).getContent();

        int totalDelivered = 0;
        int totalReported = 0;

        for (Campaign campaign : campaigns) {
            CampaignStats stats = campaign.getStats();
            if (stats != null) {
                totalDelivered += stats.getEmailsDelivered();
                totalReported += stats.getEmailsReported();
            }
        }

        return totalDelivered > 0 ? Math.round((double) totalReported / totalDelivered * 1000) / 10.0 : 0;
    }

    @Override
    public void recalculateUserRiskProfiles() {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        log.info("Recalculating user risk profiles for client: {}", clientId);

        List<UserRiskProfile> profiles = userRiskProfileRepository
                .findByClientIdOrderByRiskScoreDesc(clientId, PageRequest.of(0, 10000))
                .getContent();

        for (UserRiskProfile profile : profiles) {
            double training = profile.getTrainingRiskScore() != null ? profile.getTrainingRiskScore() : 0.0;
            double phishing = profile.getPhishingRiskScore() != null ? profile.getPhishingRiskScore() : 0.0;
            double total = RiskScoreUtils.computeOverallRiskScore(
                    training, phishing, profile.getIsTrainingEnabled(), profile.getIsPhishingEnabled(), trainingWeight, phishingWeight);
            profile.setRiskScore(total);
            profile.setRiskLevelFromScore();
            // Always push to registration so drifted AspireUser.riskGroup values are repaired.
            registrationRiskGroupSyncService.syncAlways(profile.getUserId(), profile.getRiskLevel());
        }

        userRiskProfileRepository.saveAll(profiles);
        log.info("Recalculated {} user risk profiles", profiles.size());
    }

    @Override
    public UserRiskSummaryDto getUserRiskSummary(String userId) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        
        UserRiskProfile profile = userRiskProfileRepository.findByUserId(userId)
                .orElse(null);

        return profile != null ? toUserRiskSummaryDto(profile) : null;
    }

    private UserRiskSummaryDto toUserRiskSummaryDto(UserRiskProfile profile) {
        String fullName = "";
        if (profile.getFirstName() != null) fullName += profile.getFirstName();
        if (profile.getLastName() != null) fullName += " " + profile.getLastName();

        return UserRiskSummaryDto.builder()
                .userId(profile.getUserId())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .fullName(fullName.trim())
                .department(profile.getDepartment())
                .riskLevel(profile.getRiskLevel())
                .riskScore(RiskScoreUtils.roundToTwoDecimals(profile.getRiskScore()))
                .campaignsTargeted(profile.getCampaignsTargeted())
                .emailsReceived(profile.getEmailsReceived())
                .emailsOpened(profile.getEmailsOpened())
                .emailsClicked(profile.getLinksClicked())
                .dataSubmissions(profile.getDataSubmissions())
                .emailsReported(profile.getEmailsReported())
                .breachesInvolved(profile.getBreachesInvolved())
                .isRepeatOffender(profile.isRepeatOffender())
                .lastActivityAt(profile.getLastActivityAt())
                .lastClickedAt(profile.getLastClickedAt())
                .build();
    }
}
