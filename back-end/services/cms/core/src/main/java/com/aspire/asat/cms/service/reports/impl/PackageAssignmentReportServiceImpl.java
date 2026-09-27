package com.aspire.asat.cms.service.reports.impl;

import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.registration.AspireUserBasicDto;
import com.aspire.asat.cms.dto.reports.PackageAssignmentLogRowDTO;
import com.aspire.asat.cms.dto.reports.PackageAssignmentReportDTO;
import com.aspire.asat.cms.dto.reports.PackageAssignmentSummaryDTO;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.custom.SubPackageRepositoryCustom;
import com.aspire.asat.cms.repository.custom.UserPackageRepositoryCustom;
import com.aspire.asat.cms.service.external.RegistrationServiceClient;
import com.aspire.asat.cms.service.reports.ClientReportScope;
import com.aspire.asat.cms.service.reports.ClientReportScopeResolver;
import com.aspire.asat.cms.service.reports.PackageAssignmentReportService;
import com.aspire.asat.cms.util.PackageAssignmentStatusUtil;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PackageAssignmentReportServiceImpl implements PackageAssignmentReportService {

    private static final int EXPORT_PAGE_SIZE = 1000;
    private static final int EXPORT_MAX_PAGES = 1000;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String ASSIGNED_BY = "Admin";

    private final UserSubPackageRepository userSubPackageRepository;
    private final SubPackageRepository subPackageRepository;
    private final SubPackageRepositoryCustom subPackageRepositoryCustom;
    private final UserPackageRepositoryCustom userPackageRepositoryCustom;
    private final RegistrationServiceClient registrationServiceClient;
    private final UserCurrentContextService userCurrentContextService;
    private final ClientReportScopeResolver clientReportScopeResolver;

    @Override
    public PackageAssignmentReportDTO getPackageAssignmentReport(String clientAdminId,
                                                                 String mspId,
                                                                 String search,
                                                                 String status,
                                                                 LocalDate fromDate,
                                                                 LocalDate toDate,
                                                                 int offset,
                                                                 int pageSize) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        ClientReportScope scope = clientReportScopeResolver.resolve(context, clientAdminId, mspId);

        if (scope.isEmpty()) {
            return emptyReport(offset, pageSize);
        }

        PackageAssignmentSummaryDTO summary = buildSummary(scope, fromDate, toDate);

        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;

        List<UserSubPackage> page = userSubPackageRepository.findAssignmentsForReport(
                scope.clientAdminId(), scope.clientAdminIds(), search, status,
                fromDate, toDate, safeOffset, safePageSize);
        long total = userSubPackageRepository.countAssignments(
                scope.clientAdminId(), scope.clientAdminIds(), search, status,
                fromDate, toDate);

        List<PackageAssignmentLogRowDTO> rows = mapToRows(page);

        return PackageAssignmentReportDTO.builder()
                .summary(summary)
                .assignmentLog(new AllResponseDto<>(safeOffset, safePageSize, total, rows))
                .build();
    }

    @Override
    public byte[] exportPackageAssignmentReportCsv(String clientAdminId,
                                                   String mspId,
                                                   String search,
                                                   String status,
                                                   LocalDate fromDate,
                                                   LocalDate toDate) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        ClientReportScope scope = clientReportScopeResolver.resolve(context, clientAdminId, mspId);

        if (scope.isEmpty()) {
            return buildCsv(List.of());
        }

        List<PackageAssignmentLogRowDTO> rows = fetchAllRowsForExport(
                scope, search, status, fromDate, toDate);
        return buildCsv(rows);
    }

    private PackageAssignmentSummaryDTO buildSummary(ClientReportScope scope, LocalDate fromDate, LocalDate toDate) {
        long totalSubPackages = subPackageRepositoryCustom.countSubPackagesForReport(
                scope.clientAdminId(), scope.clientAdminIds());
        long totalPackages = userSubPackageRepository.countDistinctParentPackages(
                scope.clientAdminId(), scope.clientAdminIds(), fromDate, toDate);
        long activeAssignments = userSubPackageRepository.countByExpiryBucket(
                scope.clientAdminId(), scope.clientAdminIds(), fromDate, toDate, "ACTIVE");
        long expiringSoon = userSubPackageRepository.countByExpiryBucket(
                scope.clientAdminId(), scope.clientAdminIds(), fromDate, toDate, "EXPIRING");
        long expired = userSubPackageRepository.countByExpiryBucket(
                scope.clientAdminId(), scope.clientAdminIds(), fromDate, toDate, "EXPIRED");
        long completeAssignments = userSubPackageRepository.countByExpiryBucket(
                scope.clientAdminId(), scope.clientAdminIds(), fromDate, toDate, "COMPLETE");

        return PackageAssignmentSummaryDTO.builder()
                .totalPackages(totalPackages)
                .totalSubPackages(totalSubPackages)
                .activeAssignments(activeAssignments)
                .expiringSoon(expiringSoon)
                .expired(expired)
                .completeAssignments(completeAssignments)
                .build();
    }

    private List<PackageAssignmentLogRowDTO> mapToRows(List<UserSubPackage> assignments) {
        if (assignments.isEmpty()) {
            return List.of();
        }

        Map<String, String> subPackageIdToPackageId = resolveSubPackageToPackageIdMap(assignments);
        Map<String, String> packageIdToName = resolvePackageNames(subPackageIdToPackageId.values());
        Map<String, String> userIdToName = resolveUserNames(assignments);
        LocalDate today = LocalDate.now();

        List<PackageAssignmentLogRowDTO> rows = new ArrayList<>(assignments.size());
        for (UserSubPackage assignment : assignments) {
            String packageId = subPackageIdToPackageId.get(assignment.getSubPackageId());
            String packageName = packageId != null
                    ? packageIdToName.getOrDefault(packageId, "N/A")
                    : "N/A";

            rows.add(PackageAssignmentLogRowDTO.builder()
                    .user(userIdToName.getOrDefault(assignment.getUserId(), defaultUserName(assignment)))
                    .packageName(packageName)
                    .subPackageName(assignment.getSubPackageName())
                    .assignedDate(assignment.getAssignedDate())
                    .expiryDate(assignment.getExpiryDate())
                    .status(PackageAssignmentStatusUtil.resolveDisplayStatus(
                            assignment.getAssignedDate(), assignment.getExpiryDate(),
                            assignment.getStatus(), today))
                    .assignedBy(ASSIGNED_BY)
                    .build());
        }
        return rows;
    }

    private Map<String, String> resolveSubPackageToPackageIdMap(List<UserSubPackage> assignments) {
        Set<String> subPackageIds = assignments.stream()
                .map(UserSubPackage::getSubPackageId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (subPackageIds.isEmpty()) {
            return Map.of();
        }

        Map<String, String> result = new HashMap<>();
        for (SubPackage subPackage : subPackageRepository.findAllById(subPackageIds)) {
            result.put(subPackage.getId(), subPackage.getPackageId());
        }
        return result;
    }

    private Map<String, String> resolvePackageNames(java.util.Collection<String> packageIds) {
        List<String> distinctIds = packageIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        return userPackageRepositoryCustom.getBundleNamesByPackageIds(distinctIds);
    }

    private Map<String, String> resolveUserNames(List<UserSubPackage> assignments) {
        List<String> userIds = assignments.stream()
                .map(UserSubPackage::getUserId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }

        // Keep Registration lookups bounded to report page size (including export chunks).
        Map<String, String> userIdToName = new HashMap<>();
        for (int start = 0; start < userIds.size(); start += DEFAULT_PAGE_SIZE) {
            int end = Math.min(start + DEFAULT_PAGE_SIZE, userIds.size());
            List<AspireUserBasicDto> users = registrationServiceClient.getUsersByIds(
                    userIds.subList(start, end));
            if (users == null || users.isEmpty()) {
                continue;
            }
            for (AspireUserBasicDto user : users) {
                if (user.getUserId() == null || user.getUserId().isBlank()) {
                    continue;
                }
                userIdToName.putIfAbsent(user.getUserId(), displayName(user));
            }
        }
        return userIdToName;
    }

    private String displayName(AspireUserBasicDto user) {
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            return user.getFullName().trim();
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail();
        }
        return user.getUserId();
    }

    private String defaultUserName(UserSubPackage assignment) {
        if (assignment.getUserEmail() != null && !assignment.getUserEmail().isBlank()) {
            return assignment.getUserEmail();
        }
        return assignment.getUserId() != null && !assignment.getUserId().isBlank()
                ? assignment.getUserId()
                : "N/A";
    }

    private List<PackageAssignmentLogRowDTO> fetchAllRowsForExport(ClientReportScope scope,
                                                                  String search,
                                                                  String status,
                                                                  LocalDate fromDate,
                                                                  LocalDate toDate) {
        List<PackageAssignmentLogRowDTO> allRows = new ArrayList<>();
        for (int page = 0; page < EXPORT_MAX_PAGES; page++) {
            List<UserSubPackage> chunk = userSubPackageRepository.findAssignmentsForReportExport(
                    scope.clientAdminId(), scope.clientAdminIds(), search, status,
                    fromDate, toDate, page, EXPORT_PAGE_SIZE);
            if (chunk.isEmpty()) {
                break;
            }
            allRows.addAll(mapToRows(chunk));
            if (chunk.size() < EXPORT_PAGE_SIZE) {
                break;
            }
        }
        return allRows;
    }

    private PackageAssignmentReportDTO emptyReport(int offset, int pageSize) {
        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        return PackageAssignmentReportDTO.builder()
                .summary(PackageAssignmentSummaryDTO.builder()
                        .totalPackages(0L)
                        .totalSubPackages(0L)
                        .activeAssignments(0L)
                        .expiringSoon(0L)
                        .expired(0L)
                        .completeAssignments(0L)
                        .build())
                .assignmentLog(new AllResponseDto<>(safeOffset, safePageSize, 0L, List.of()))
                .build();
    }

    private static byte[] buildCsv(List<PackageAssignmentLogRowDTO> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append("User,Package,Sub Package,Assigned,Expiry,Status,Assigned By\n");
        for (PackageAssignmentLogRowDTO row : rows) {
            sb.append(escapeCsv(row.getUser())).append(',');
            sb.append(escapeCsv(row.getPackageName())).append(',');
            sb.append(escapeCsv(row.getSubPackageName())).append(',');
            sb.append(row.getAssignedDate() != null ? row.getAssignedDate() : "").append(',');
            sb.append(row.getExpiryDate() != null ? row.getExpiryDate() : "").append(',');
            sb.append(escapeCsv(row.getStatus())).append(',');
            sb.append(escapeCsv(row.getAssignedBy())).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
