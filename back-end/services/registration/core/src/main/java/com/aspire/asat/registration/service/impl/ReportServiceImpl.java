package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.enums.RiskGroup;
import com.aspire.asat.registration.data.reports.UserDetailRowDTO;
import com.aspire.asat.registration.data.reports.UserGrowthTrendPointDTO;
import com.aspire.asat.registration.data.reports.UserSummaryReportDTO;
import com.aspire.asat.registration.data.reports.UserSummaryTotalsDTO;
import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.service.ReportService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Composes the User Summary Report from {@link AspireUserRepository} (counts,
 * growth trend and the filtered details list) and renders the User Details
 * slice as a CSV file using the same BOM + escape pattern as
 * {@code MspClientLicenseServiceImpl}.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_SUSPENDED = "SUSPEND";
    private static final int DEFAULT_TREND_MONTHS = 6;
    private static final int EXPORT_PAGE_SIZE = 1000;
    private static final int EXPORT_MAX_PAGES = 1000;

    private final AspireUserRepository aspireUserRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public UserSummaryReportDTO getUserSummaryReport(String clientAdminId,
                                                    String mspId,
                                                    String search,
                                                    String status,
                                                    String userType,
                                                    String country,
                                                    Instant fromDate,
                                                    Instant toDateExclusive,
                                                    int offset,
                                                    int pageSize,
                                                    int trendMonths) {
        log.info("Building user summary report - clientAdminId: {}, mspId: {}, search: {}, status: {}, userType: {}, country: {}, fromDate: {}, toDateExclusive: {}, offset: {}, pageSize: {}, trendMonths: {}",
                clientAdminId, mspId, search, status, userType, country, fromDate, toDateExclusive, offset, pageSize, trendMonths);

        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        ReportScope scope = resolveReportScope(context, clientAdminId, mspId);

        if (scope.isEmpty()) {
            return emptyUserSummaryReport(offset, pageSize);
        }

        int effectiveTrendMonths = trendMonths <= 0 ? DEFAULT_TREND_MONTHS : trendMonths;
        UserSummaryTotalsDTO totals = buildTotals(scope);
        List<UserGrowthTrendPointDTO> trend = scope.isMulti()
                ? aspireUserRepository.getUserGrowthTrend(effectiveTrendMonths, scope.clientAdminIds())
                : aspireUserRepository.getUserGrowthTrend(effectiveTrendMonths, scope.clientAdminId(), scope.mspId());

        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? 20 : pageSize;

        List<AspireUser> page;
        long total;
        if (scope.isMulti()) {
            page = aspireUserRepository.findUsersForReport(
                    search, userType, country, scope.clientAdminIds(), status,
                    fromDate, toDateExclusive, safeOffset, safePageSize);
            total = aspireUserRepository.countUsersForReport(
                    search, userType, country, scope.clientAdminIds(), status,
                    fromDate, toDateExclusive);
        } else {
            page = aspireUserRepository.findUsersForReport(
                    search, userType, country, scope.mspId(), scope.clientAdminId(), status,
                    fromDate, toDateExclusive, safeOffset, safePageSize);
            total = aspireUserRepository.countUsersForReport(
                    search, userType, country, scope.mspId(), scope.clientAdminId(), status,
                    fromDate, toDateExclusive);
        }

        List<UserDetailRowDTO> rows = new ArrayList<>(page.size());
        for (AspireUser u : page) {
            rows.add(toRow(u));
        }

        AllResponseDto<List<UserDetailRowDTO>> details =
                new AllResponseDto<>(safeOffset, safePageSize, total, rows);

        return UserSummaryReportDTO.builder()
                .totals(totals)
                .growthTrend(trend)
                .details(details)
                .build();
    }

    @Override
    public byte[] exportUserDetailsCsv(String clientAdminId,
                                       String mspId,
                                       String search,
                                       String status,
                                       String userType,
                                       String country,
                                       Instant fromDate,
                                       Instant toDateExclusive) {
        log.info("Exporting user summary CSV - clientAdminId: {}, mspId: {}, search: {}, status: {}, userType: {}, country: {}, fromDate: {}, toDateExclusive: {}",
                clientAdminId, mspId, search, status, userType, country, fromDate, toDateExclusive);

        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        ReportScope scope = resolveReportScope(context, clientAdminId, mspId);

        if (scope.isEmpty()) {
            return buildCsv(List.of());
        }

        List<AspireUser> all = fetchAllForExport(scope, search, status, userType, country, fromDate, toDateExclusive);
        return buildCsv(all);
    }

    private ReportScope resolveReportScope(CurrentUserContext context, String requestedClientAdminId, String requestedMspId) {
        UserType userType = UserType.fromString(context.getUserType());

        if (UserType.CLIENT_ADMIN.equals(userType)) {
            return ReportScope.single(resolveClientAdminScope(context, requestedClientAdminId), null);
        }

        if (isMspReportRequest(context)) {
            List<String> clientAdminIds = resolveMspClientAdminIds(context, requestedClientAdminId, requestedMspId);
            if (clientAdminIds.isEmpty()) {
                return ReportScope.empty();
            }
            if (clientAdminIds.size() == 1) {
                return ReportScope.single(clientAdminIds.get(0), null);
            }
            return ReportScope.multi(clientAdminIds);
        }

        return ReportScope.single(requestedClientAdminId, requestedMspId);
    }

    private String resolveClientAdminScope(CurrentUserContext context, String requestedClientAdminId) {
        UserType userType = UserType.fromString(context.getUserType());
        if (UserType.CLIENT_ADMIN.equals(userType)) {
            return context.getClientAdminId();
        }
        return requestedClientAdminId;
    }

    private List<String> resolveMspClientAdminIds(CurrentUserContext context, String clientAdminId, String mspId) {
        String normalizedClientAdminId = (clientAdminId != null && !clientAdminId.isBlank()) ? clientAdminId.trim() : null;
        if (normalizedClientAdminId != null) {
            return List.of(normalizedClientAdminId);
        }

        List<String> contextClientAdminIds = context.getClientAdminIds();
        if (contextClientAdminIds != null && !contextClientAdminIds.isEmpty()) {
            return contextClientAdminIds;
        }

        log.warn("Unable to resolve client admin IDs for MSP userId={}, userType={}",
                context.getUserId(), context.getUserType());
        return List.of();
    }

    private boolean isMspReportRequest(CurrentUserContext context) {
        try {
            return UserType.MSP.equals(UserType.fromString(context.getUserType()));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private UserSummaryReportDTO emptyUserSummaryReport(int offset, int pageSize) {
        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? 20 : pageSize;
        return UserSummaryReportDTO.builder()
                .totals(UserSummaryTotalsDTO.builder()
                        .totalUsers(0L)
                        .activeUsers(0L)
                        .suspendedUsers(0L)
                        .newSignupsLast30Days(0L)
                        .build())
                .growthTrend(List.of())
                .details(new AllResponseDto<>(safeOffset, safePageSize, 0L, List.of()))
                .build();
    }

    private UserSummaryTotalsDTO buildTotals(ReportScope scope) {
        if (scope.isMulti()) {
            long totalUsers = aspireUserRepository.countAllScoped(scope.clientAdminIds());
            long activeUsers = aspireUserRepository.countByStatusScoped(STATUS_ACTIVE, scope.clientAdminIds());
            long suspendedUsers = aspireUserRepository.countByStatusScoped(STATUS_SUSPENDED, scope.clientAdminIds());
            Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
            long newSignupsLast30Days = aspireUserRepository.countUsersCreatedSince(since, scope.clientAdminIds());
            return UserSummaryTotalsDTO.builder()
                    .totalUsers(totalUsers)
                    .activeUsers(activeUsers)
                    .suspendedUsers(suspendedUsers)
                    .newSignupsLast30Days(newSignupsLast30Days)
                    .build();
        }

        boolean systemWide = isBlank(scope.clientAdminId()) && isBlank(scope.mspId());
        long totalUsers = systemWide
                ? aspireUserRepository.count()
                : aspireUserRepository.countAllScoped(scope.clientAdminId(), scope.mspId());
        long activeUsers = systemWide
                ? aspireUserRepository.countByStatus(STATUS_ACTIVE)
                : aspireUserRepository.countByStatusScoped(STATUS_ACTIVE, scope.clientAdminId(), scope.mspId());
        long suspendedUsers = systemWide
                ? aspireUserRepository.countByStatus(STATUS_SUSPENDED)
                : aspireUserRepository.countByStatusScoped(STATUS_SUSPENDED, scope.clientAdminId(), scope.mspId());
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        long newSignupsLast30Days = aspireUserRepository.countUsersCreatedSince(since, scope.clientAdminId(), scope.mspId());

        return UserSummaryTotalsDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .suspendedUsers(suspendedUsers)
                .newSignupsLast30Days(newSignupsLast30Days)
                .build();
    }

    /**
     * Pulls users in chunks (page size 1000) until the underlying query is exhausted.
     * Pagination here uses page-number semantics (skip = offset * pageSize).
     */
    private List<AspireUser> fetchAllForExport(ReportScope scope,
                                               String search,
                                               String status,
                                               String userType,
                                               String country,
                                               Instant fromDate,
                                               Instant toDateExclusive) {
        List<AspireUser> all = new ArrayList<>();
        for (int page = 0; page < EXPORT_MAX_PAGES; page++) {
            List<AspireUser> chunk;
            if (scope.isMulti()) {
                chunk = aspireUserRepository.findUsersForReport(
                        search, userType, country, scope.clientAdminIds(), status,
                        fromDate, toDateExclusive, page, EXPORT_PAGE_SIZE);
            } else {
                chunk = aspireUserRepository.findUsersForReport(
                        search, userType, country, scope.mspId(), scope.clientAdminId(), status,
                        fromDate, toDateExclusive, page, EXPORT_PAGE_SIZE);
            }
            if (chunk.isEmpty()) {
                break;
            }
            all.addAll(chunk);
            if (chunk.size() < EXPORT_PAGE_SIZE) {
                break;
            }
        }
        return all;
    }

    private static UserDetailRowDTO toRow(AspireUser u) {
        return UserDetailRowDTO.builder()
                .name(fullName(u.getFirstName(), u.getLastName()))
                .email(u.getEmail() != null ? u.getEmail() : u.getUsername())
                .department(u.getDepartment())
                .riskGroup(u.getRiskGroup())
                .role(u.getUserType())
                .status(u.getStatus())
                .lastLoginAt(u.getLastLoginAt())
                .createdAt(u.getCreatedAt())
                .build();
    }

    private static String fullName(String first, String last) {
        String f = first == null ? "" : first.trim();
        String l = last == null ? "" : last.trim();
        if (f.isEmpty() && l.isEmpty()) {
            return "";
        }
        if (f.isEmpty()) {
            return l;
        }
        if (l.isEmpty()) {
            return f;
        }
        return f + " " + l;
    }

    private static byte[] buildCsv(List<AspireUser> users) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("Name,Email,Department,Risk Group,Role,Status,Last Login,Created At\n");

        for (AspireUser u : users) {
            sb.append(escapeCsv(fullName(u.getFirstName(), u.getLastName())));
            sb.append(',');
            sb.append(escapeCsv(u.getEmail() != null ? u.getEmail() : u.getUsername()));
            sb.append(',');
            sb.append(escapeCsv(u.getDepartment()));
            sb.append(',');
            sb.append(escapeCsv(riskGroupLabel(u.getRiskGroup())));
            sb.append(',');
            sb.append(escapeCsv(u.getUserType()));
            sb.append(',');
            sb.append(escapeCsv(u.getStatus()));
            sb.append(',');
            sb.append(u.getLastLoginAt() != null ? u.getLastLoginAt().toString() : "");
            sb.append(',');
            sb.append(u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
            sb.append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String riskGroupLabel(RiskGroup riskGroup) {
        return riskGroup == null ? "" : riskGroup.name();
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private record ReportScope(String clientAdminId, String mspId, List<String> clientAdminIds) {
        static ReportScope single(String clientAdminId, String mspId) {
            return new ReportScope(clientAdminId, mspId, null);
        }

        static ReportScope multi(List<String> clientAdminIds) {
            return new ReportScope(null, null, clientAdminIds);
        }

        static ReportScope empty() {
            return new ReportScope(null, null, Collections.emptyList());
        }

        boolean isMulti() {
            return clientAdminIds != null && clientAdminIds.size() > 1;
        }

        boolean isEmpty() {
            return clientAdminIds != null && clientAdminIds.isEmpty();
        }
    }
}
