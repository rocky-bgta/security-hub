package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.client.PhishingClient;
import com.aspire.asat.cms.client.dto.PhishingTrainingRiskScoreRequestDto;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.service.TrainingRiskScoreSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Syncs training risk score from CMS UserSubPackage data to the Phishing module via PhishingClient.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingRiskScoreSyncServiceImpl implements TrainingRiskScoreSyncService {

    private final UserSubPackageRepository userSubPackageRepository;
    private final PhishingClient phishingClient;

    @Override
    public Optional<Double> syncTrainingRiskScore(String previousStatus, UserSubPackage updatedUserSubPackage) {
        if (updatedUserSubPackage == null) {
            log.warn("syncTrainingRiskScore: updatedUserSubPackage is required");
            return Optional.empty();
        }

        if (Boolean.TRUE.equals(updatedUserSubPackage.getIsPhishingSubpackage())) {
            log.debug("syncTrainingRiskScore: skipping phishing subpackage for userId={}, clientAdminId={}",
                    updatedUserSubPackage.getUserId(), updatedUserSubPackage.getClientAdminId());
            return Optional.empty();
        }

        String newStatus = updatedUserSubPackage.getStatus();
        if (Objects.equals(previousStatus, newStatus)) {
            log.debug("syncTrainingRiskScore: status unchanged ({}), skipping sync for userId={}, clientAdminId={}",
                    newStatus, updatedUserSubPackage.getUserId(), updatedUserSubPackage.getClientAdminId());
            return Optional.empty();
        }

        String userId = updatedUserSubPackage.getUserId();
        String clientAdminId = updatedUserSubPackage.getClientAdminId();

        if (userId == null || userId.isBlank()) {
            log.warn("syncTrainingRiskScore: userId is required");
            return Optional.empty();
        }
        if (clientAdminId == null || clientAdminId.isBlank()) {
            log.warn("syncTrainingRiskScore: clientAdminId is required");
            return Optional.empty();
        }

        List<UserSubPackage> userSubPackages =
                userSubPackageRepository.findByUserIdAndClientAdminIdAndIsPhishingSubpackageNotTrue(userId, clientAdminId);

        if (userSubPackages.isEmpty()) {
            log.debug("syncTrainingRiskScore: no userSubPackages for userId={}, clientAdminId={}", userId, clientAdminId);
            return Optional.empty();
        }

        double summedRiskScore = 0.0;
        for (UserSubPackage usp : userSubPackages) {
            if (usp.getRiskScore() != null) {
                summedRiskScore += usp.getRiskScore();
            }
        }
        double averageScore = summedRiskScore / userSubPackages.size();
        // Alternative with stream: double summedRiskScore = userSubPackages.stream().mapToDouble(UserSubPackage::getRiskScore).sum();

        log.debug("syncTrainingRiskScore: userId={}, clientAdminId={}, subPackageCount={}, summedRiskScore={}",
                userId, clientAdminId, userSubPackages.size(), averageScore);

        PhishingTrainingRiskScoreRequestDto request = new PhishingTrainingRiskScoreRequestDto(clientAdminId, averageScore);
        return phishingClient.updateTrainingRiskScore(userId, request);
    }
}
