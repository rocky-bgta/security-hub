package com.aspire.asat.billing.service;

import com.aspire.asat.billing.dto.analytics.*;

import java.util.List;

/**
 * Service interface for billing analytics operations.
 */
public interface BillingAnalyticsService {

    /**
     * Get complete billing analytics data.
     */
    BillingAnalyticsResponseDTO getAnalytics(AnalyticsPeriod period, String startDate, String endDate, 
                                              String mspId, String countryId);

    /**
     * Get summary metrics only.
     */
    BillingAnalyticsSummaryDTO getSummary(AnalyticsPeriod period, String startDate, String endDate, 
                                           String mspId, String countryId);

    /**
     * Get revenue trend data.
     */
    List<RevenueByPeriodDTO> getRevenueTrend(AnalyticsPeriod period, String startDate, String endDate, 
                                              String mspId, String countryId);

    /**
     * Get top performing packages.
     */
    List<TopPackageDTO> getTopPackages(String startDate, String endDate, String mspId, String countryId, int limit);

    /**
     * Get failed payments analysis.
     */
    List<FailedPaymentAnalysisDTO> getFailedPaymentsAnalysis(String startDate, String endDate, 
                                                              String mspId, String countryId);

    /**
     * Get payment success rate metrics.
     */
    PaymentSuccessRateDTO getPaymentSuccessRate(String startDate, String endDate, String mspId, String countryId);

    /**
     * Export analytics data as CSV.
     */
    byte[] exportAnalyticsCsv(AnalyticsPeriod period, String startDate, String endDate, 
                              String mspId, String countryId);

    /**
     * Export analytics data as PDF.
     */
    byte[] exportAnalyticsPdf(AnalyticsPeriod period, String startDate, String endDate, 
                              String mspId, String countryId);
}

