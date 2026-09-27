package com.aspire.asat.billing.repo.customRepo;

import com.aspire.asat.billing.dto.analytics.*;

import java.time.Instant;
import java.util.List;

/**
 * Custom repository interface for billing analytics aggregation queries.
 */
public interface BillingAnalyticsRepositoryCustom {

    /**
     * Get total revenue from paid invoices within date range.
     */
    Double getTotalRevenue(Instant startDate, Instant endDate, String mspId, String countryId);

    /**
     * Get total refunds within date range.
     */
    Double getTotalRefunds(Instant startDate, Instant endDate, String mspId, String countryId);

    /**
     * Get revenue data grouped by period (month/quarter/year).
     */
    List<RevenueByPeriodDTO> getRevenueByPeriod(AnalyticsPeriod period, Instant startDate, Instant endDate, String mspId, String countryId);

    /**
     * Get count of new subscriptions (invoices) within date range.
     */
    Long getNewSubscriptionsCount(Instant startDate, Instant endDate, String mspId, String countryId);

    /**
     * Get total active licenses count from paid invoices.
     */
    Long getActiveLicensesCount(String mspId, String countryId);

    /**
     * Get top performing packages with revenue and subscription count.
     */
    List<TopPackageDTO> getTopPackages(Instant startDate, Instant endDate, String mspId, String countryId, int limit);

    /**
     * Get failed payments breakdown by reason.
     */
    List<FailedPaymentAnalysisDTO> getFailedPaymentsByReason(Instant startDate, Instant endDate, String mspId, String countryId);

    /**
     * Get payment success rate metrics.
     */
    PaymentSuccessRateDTO getPaymentSuccessRate(Instant startDate, Instant endDate, String mspId, String countryId);

    /**
     * Get failed payments count within date range.
     */
    Long getFailedPaymentsCount(Instant startDate, Instant endDate, String mspId, String countryId);
}

