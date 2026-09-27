package com.aspire.asat.cms.service;

import com.aspire.asat.cms.model.UserSubPackage;

import java.util.Optional;

/**
 * Service to sync training risk score from CMS UserSubPackage data to the Phishing module.
 */
public interface TrainingRiskScoreSyncService {

    /**
     * Syncs training risk score to the Phishing module when UserSubPackage status has changed.
     * Phishing subpackages are skipped (risk score stays null and is excluded from the average).
     * If previousStatus equals the updated entity's status, the sync is skipped.
     * Otherwise loads non-phishing UserSubPackage records for the user and client admin, averages their risk scores,
     * and calls the Phishing client to update the training risk score.
     *
     * @param previousStatus       status before the update (null for new assignments)
     * @param updatedUserSubPackage the UserSubPackage after update (must not be null)
     * @return optional containing the updated risk score returned by Phishing if the call succeeded, empty otherwise
     */
    Optional<Double> syncTrainingRiskScore(String previousStatus, UserSubPackage updatedUserSubPackage);
}
