package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.reports.UserSummaryReportDTO;

import java.time.Instant;

/**
 * Service that backs the User Summary Report endpoint and its CSV export.
 * The scoping params are optional: when both {@code clientAdminId} and
 * {@code mspId} are blank the report is system-wide, otherwise it is filtered
 * to that organisation. {@code fromDate}/{@code toDateExclusive} apply to the
 * {@code AspireUser.createdAt} field and filter the User Details list / CSV
 * export.
 */
public interface ReportService {

    /**
     * Build the full User Summary Report payload (totals, growth trend and a
     * paginated slice of the user details table). The date range filters the
     * details table only; totals and the growth trend remain anchored to "now".
     */
    UserSummaryReportDTO getUserSummaryReport(
            String clientAdminId,
            String mspId,
            String search,
            String status,
            String userType,
            String country,
            Instant fromDate,
            Instant toDateExclusive,
            int offset,
            int pageSize,
            int trendMonths);

    /**
     * Build a CSV (UTF-8 with BOM) of the User Details rows for the same filters.
     * The export is not paginated; all matching users are written into the file.
     */
    byte[] exportUserDetailsCsv(
            String clientAdminId,
            String mspId,
            String search,
            String status,
            String userType,
            String country,
            Instant fromDate,
            Instant toDateExclusive);
}
