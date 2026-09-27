package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.reports.QuickRange;
import com.aspire.asat.registration.data.reports.SubscriptionDetailRowDTO;
import com.aspire.asat.registration.data.reports.SubscriptionSummaryTotalsDTO;

import java.time.Instant;
import java.util.List;

/**
 * Backs the Subscription Summary Report endpoints (summary cards, paginated
 * detail list and CSV export) over {@code ClientProduct} data.
 * <p>
 * All filter arguments are optional. Callers are auto-scoped by the current user
 * context: a CLIENT_ADMIN is restricted to their own {@code clientAdminId} and an
 * MSP to their own {@code mspId}; system users may pass explicit scoping filters.
 * When supplied, {@code quickRange} takes precedence over {@code fromDate}/
 * {@code toDateExclusive}, which otherwise filter on {@code ClientProduct.assignedAt}.
 */
public interface SubscriptionSummaryReportService {

    /**
     * Aggregate license counts for the summary cards over the filtered set.
     */
    SubscriptionSummaryTotalsDTO getSummary(
            String search,
            String clientAdminId,
            String mspId,
            String status,
            Instant fromDate,
            Instant toDateExclusive,
            QuickRange quickRange);

    /**
     * Paginated subscription detail rows for the filtered set.
     */
    AllResponseDto<List<SubscriptionDetailRowDTO>> getDetailList(
            String search,
            String clientAdminId,
            String mspId,
            String status,
            Instant fromDate,
            Instant toDateExclusive,
            QuickRange quickRange,
            int offset,
            int pageSize);

    /**
     * CSV (UTF-8 with BOM) of every detail row for the filtered set (not paginated).
     */
    byte[] exportCsv(
            String search,
            String clientAdminId,
            String mspId,
            String status,
            Instant fromDate,
            Instant toDateExclusive,
            QuickRange quickRange);
}
