package com.aspire.asat.phishing.service;

/**
 * Service for persisting training risk score and updating UserRiskProfile.
 */
public interface TrainingRiskScoreService {

    /**
     * Updates UserRiskProfile.trainingRiskScore with the given riskScore for the user and client.
     * If phishingRiskScore exists, overall riskScore is updated as weighted (30% training, 70% phishing).
     *
     * @param userId       user id
     * @param clientAdminId client/admin id (from request body)
     * @param riskScore    training risk score to store
     * @return the stored training risk score
     */
    double updateTrainingRiskScore(String userId, String clientAdminId, Double riskScore);
}
