package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.clientDashboard.UserRiskAnalysisResponseDto;
import com.aspire.asat.cms.dto.clientDashboard.UserRiskDetailDto;
import com.aspire.asat.cms.dto.enums.RiskCategory;

import java.util.List;

/**
 * Service interface for User Risk Analysis
 * Provides business logic for analyzing user risk based on training completion progress
 */
public interface UserRiskAnalysisService {

    /**
     * Generates a comprehensive risk analysis report for all users under a specific client admin
     * 
     * @param clientAdminId The client admin ID to analyze users for
     * @param productId Optional product ID to filter by. If null, analyzes all products
     * @return UserRiskAnalysisResponseDto containing risk categorization counts
     */
    UserRiskAnalysisResponseDto generateUserRiskAnalysis(String clientAdminId, String productId);

    /**
     * Gets detailed risk analysis for individual users under a specific client admin
     * 
     * @param clientAdminId The client admin ID to analyze users for
     * @param riskCategory Optional filter by risk category
     * @param offset Page offset for pagination
     * @param pageSize Page size for pagination
     * @return List of UserRiskDetailDto containing detailed user risk information
     */
    List<UserRiskDetailDto> getUserRiskAnalysisDetails(String clientAdminId, RiskCategory riskCategory, Integer offset, Integer pageSize);

    /**
     * Exports user risk analysis report as CSV
     * 
     * @param clientAdminId The client admin ID to export data for
     * @return Download URL for the exported CSV file
     */
    String exportUserRiskAnalysis(String clientAdminId);

    /**
     * Calculates overall progress for a user based on their sub-package completions
     * 
     * @param userId The user ID to calculate progress for
     * @param clientAdminId The client admin ID for context
     * @param productId Optional product ID to filter by. If null, includes all products
     * @return Overall progress percentage (0-100)
     */
    Double calculateUserOverallProgress(String userId, String clientAdminId, String productId);

    /**
     * Determines risk category based on overall progress percentage
     * 
     * @param progressPercentage The overall progress percentage (0-100)
     * @return Risk category enum
     */
    RiskCategory determineRiskCategory(Double progressPercentage);
}
