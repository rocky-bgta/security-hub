package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.response.UserRiskSummaryDto;

import java.util.List;

/**
 * Service interface for analytics calculations.
 */
public interface AnalyticsService {

    /**
     * Calculate phish-prone percentage
     * BR-02: Phish-prone % = (clicked / sent) * 100
     */
    double getPhishPronePercentage();

    /**
     * Get phish-prone percentage for specific campaign
     */
    double getPhishPronePercentageForCampaign(String campaignId);

    /**
     * Get repeat offenders (clicked 3+ times)
     * BR-04: Repeat offender = clicked 3+ times
     */
    List<UserRiskSummaryDto> getRepeatOffenders(int limit);

    /**
     * Get email delivery rate
     */
    double getDeliveryRate();

    /**
     * Get user compromise rate
     */
    double getCompromiseRate();

    /**
     * Get phishing report rate
     */
    double getReportRate();

    /**
     * Calculate and update user risk profiles
     */
    void recalculateUserRiskProfiles();

    /**
     * Get user risk summary by user ID
     */
    UserRiskSummaryDto getUserRiskSummary(String userId);
}
