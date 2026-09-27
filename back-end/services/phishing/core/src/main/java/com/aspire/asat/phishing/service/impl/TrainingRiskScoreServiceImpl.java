package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.service.TrainingRiskScoreService;
import com.aspire.asat.phishing.service.support.RegistrationRiskGroupSyncService;
import com.aspire.asat.phishing.utils.RiskScoreUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Persists training risk score and updates UserRiskProfile.
 * Scenario A (training only): riskScore = trainingRiskScore (0–100).
 * Scenario B (phishing only): riskScore = phishingRiskScore (0–100).
 * Scenario C (both): 25/75 split — training contributes max 25 pts, phishing max 75 pts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingRiskScoreServiceImpl implements TrainingRiskScoreService {

    private final UserRiskProfileRepository userRiskProfileRepository;
    private final RegistrationRiskGroupSyncService registrationRiskGroupSyncService;

    @Value("${risk-score.training-weight}")
    private Double trainingWeight;

    @Value("${risk-score.phishing-weight}")
    private Double phishingWeight;

    @Override
    public double updateTrainingRiskScore(String userId, String clientAdminId, Double riskScore) {
        persistTrainingRiskScore(userId, clientAdminId, riskScore);
        log.debug("Updated trainingRiskScore={} for userId={}, clientAdminId={}", riskScore, userId, clientAdminId);
        return riskScore;
    }

    private void persistTrainingRiskScore(String userId, String clientId, Double trainingRiskScore) {
        var existingOpt = userRiskProfileRepository.findByClientIdAndUserId(clientId, userId);
        RiskLevel previousLevel = existingOpt.map(UserRiskProfile::getRiskLevel).orElse(null);

        UserRiskProfile profile = existingOpt.orElseGet(() -> {
            UserRiskProfile p = new UserRiskProfile();
            p.setId(UUID.randomUUID().toString());
            p.setClientId(clientId);
            p.setUserId(userId);
            return p;
        });
        profile.setTrainingRiskScore(trainingRiskScore);
        profile.setUpdatedAt(Instant.now());

        double phishing = profile.getPhishingRiskScore() != null ? profile.getPhishingRiskScore() : 0.0;
        double training = trainingRiskScore != null ? trainingRiskScore : 0.0;
        double total = RiskScoreUtils.computeOverallRiskScore(training, phishing,
                profile.getIsTrainingEnabled(), profile.getIsPhishingEnabled(), trainingWeight, phishingWeight);
        profile.setRiskScore(total);
        profile.setRiskLevelFromScore();
        log.debug("riskScore={} (training={}, phishing={}) for userId={}", total, trainingRiskScore, phishing, userId);

        userRiskProfileRepository.save(profile);
        registrationRiskGroupSyncService.syncIfNeeded(userId, previousLevel, profile.getRiskLevel());
    }
}
