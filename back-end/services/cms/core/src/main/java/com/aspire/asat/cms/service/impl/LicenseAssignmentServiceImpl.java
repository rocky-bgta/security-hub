package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.license.LicenseAssignmentExportDto;
import com.aspire.asat.cms.dto.license.LicenseAssignmentRowDto;
import com.aspire.asat.cms.dto.registration.AspireUserBasicDto;
import com.aspire.asat.cms.model.Product;
import com.aspire.asat.cms.model.ProductPackage;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.ProductPackageRepository;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.custom.UserPackageRepositoryCustom;
import com.aspire.asat.cms.service.LicenseAssignmentService;
import com.aspire.asat.cms.service.external.RegistrationServiceClient;
import com.aspire.asat.cms.service.reports.ClientReportScope;
import com.aspire.asat.cms.service.reports.ClientReportScopeResolver;
import com.aspire.asat.cms.util.PackageAssignmentStatusUtil;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
public class LicenseAssignmentServiceImpl implements LicenseAssignmentService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int EXPORT_PAGE_SIZE = 1000;
    private static final int EXPORT_MAX_PAGES = 1000;
    private static final String FALLBACK_NAME = "N/A";
    private static final String CSV_CONTENT_TYPE = "text/csv; charset=UTF-8";
    private static final String XLS_CONTENT_TYPE = "application/vnd.ms-excel";
    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String[] EXPORT_HEADERS = {
            "User Name", "Email", "Department", "Package", "Product", "Assigned Date", "Expiry Date", "Status"
    };

    private final UserSubPackageRepository userSubPackageRepository;
    private final SubPackageRepository subPackageRepository;
    private final ProductPackageRepository productPackageRepository;
    private final ProductRepository productRepository;
    private final UserPackageRepositoryCustom userPackageRepositoryCustom;
    private final RegistrationServiceClient registrationServiceClient;
    private final UserCurrentContextService userCurrentContextService;
    private final ClientReportScopeResolver clientReportScopeResolver;

    @Override
    public AllResponseDto<List<LicenseAssignmentRowDto>> listLicenseAssignments(
            String clientAdminId,
            String mspId,
            String search,
            String packageId,
            String productId,
            String department,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            int offset,
            int pageSize) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        ClientReportScope scope = clientReportScopeResolver.resolve(context, clientAdminId, mspId);

        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        if (scope.isEmpty()) {
            return new AllResponseDto<>(safeOffset, safePageSize, 0L, List.of());
        }

        List<String> userIds = resolveUserIdFilter(scope, search, department);
        if (userIds != null && userIds.isEmpty()) {
            return new AllResponseDto<>(safeOffset, safePageSize, 0L, List.of());
        }

        long total = userSubPackageRepository.countLicenseAssignments(
                scope.clientAdminId(), scope.clientAdminIds(), userIds, productId, packageId,
                status, fromDate, toDate);
        List<UserSubPackage> page = userSubPackageRepository.findLicenseAssignments(
                scope.clientAdminId(), scope.clientAdminIds(), userIds, productId, packageId,
                status, fromDate, toDate, safeOffset, safePageSize);

        return new AllResponseDto<>(safeOffset, safePageSize, total, mapToRows(page));
    }

    @Override
    public LicenseAssignmentExportDto exportLicenseAssignments(
            String clientAdminId,
            String mspId,
            String search,
            String packageId,
            String productId,
            String department,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            String format) {
        String normalizedFormat = format == null ? "csv" : format.trim().toLowerCase();
        if (!normalizedFormat.equals("csv") && !normalizedFormat.equals("xls") && !normalizedFormat.equals("xlsx")) {
            throw new IllegalArgumentException("Unsupported export format: " + format + ". Use csv, xls, or xlsx.");
        }

        List<LicenseAssignmentRowDto> rows = fetchAllRowsForExport(
                clientAdminId, mspId, search, packageId, productId, department, status, fromDate, toDate);

        return switch (normalizedFormat) {
            case "xls" -> LicenseAssignmentExportDto.builder()
                    .content(buildExcel(rows, true))
                    .filename("license-assignments.xls")
                    .contentType(XLS_CONTENT_TYPE)
                    .build();
            case "xlsx" -> LicenseAssignmentExportDto.builder()
                    .content(buildExcel(rows, false))
                    .filename("license-assignments.xlsx")
                    .contentType(XLSX_CONTENT_TYPE)
                    .build();
            default -> LicenseAssignmentExportDto.builder()
                    .content(buildCsv(rows))
                    .filename("license-assignments.csv")
                    .contentType(CSV_CONTENT_TYPE)
                    .build();
        };
    }

    private List<LicenseAssignmentRowDto> fetchAllRowsForExport(
            String clientAdminId,
            String mspId,
            String search,
            String packageId,
            String productId,
            String department,
            String status,
            LocalDate fromDate,
            LocalDate toDate) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        ClientReportScope scope = clientReportScopeResolver.resolve(context, clientAdminId, mspId);
        if (scope.isEmpty()) {
            return List.of();
        }

        List<String> userIds = resolveUserIdFilter(scope, search, department);
        if (userIds != null && userIds.isEmpty()) {
            return List.of();
        }

        List<LicenseAssignmentRowDto> allRows = new ArrayList<>();
        for (int page = 0; page < EXPORT_MAX_PAGES; page++) {
            List<UserSubPackage> chunk = userSubPackageRepository.findLicenseAssignments(
                    scope.clientAdminId(), scope.clientAdminIds(), userIds, productId, packageId,
                    status, fromDate, toDate, page, EXPORT_PAGE_SIZE);
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

    private List<String> resolveUserIdFilter(ClientReportScope scope, String search, String department) {
        if (!StringUtils.hasText(search) && !StringUtils.hasText(department)) {
            return null;
        }
        List<String> scopedClientAdminIds = scopeClientAdminIds(scope);
        if (scopedClientAdminIds.isEmpty()) {
            return null;
        }
        return registrationServiceClient.findEndUserIds(scopedClientAdminIds, search, department);
    }

    private List<LicenseAssignmentRowDto> mapToRows(List<UserSubPackage> assignments) {
        if (assignments.isEmpty()) {
            return List.of();
        }

        Map<String, String> subPackageIdToPackageId = resolveSubPackageToPackageIdMap(assignments);
        Map<String, String> packageIdToName = resolvePackageNames(subPackageIdToPackageId.values());
        Map<String, String> productIdToName = resolveProductNames(assignments);
        Map<String, AspireUserBasicDto> usersById = resolveUsers(assignments);
        LocalDate today = LocalDate.now();

        List<LicenseAssignmentRowDto> rows = new ArrayList<>(assignments.size());
        for (UserSubPackage assignment : assignments) {
            String resolvedPackageId = subPackageIdToPackageId.get(assignment.getSubPackageId());
            AspireUserBasicDto user = assignment.getUserId() != null
                    ? usersById.get(assignment.getUserId())
                    : null;

            rows.add(LicenseAssignmentRowDto.builder()
                    .userId(assignment.getUserId())
                    .fullName(resolveFullName(user, assignment))
                    .email(resolveEmail(user, assignment))
                    .department(user != null ? user.getDepartment() : null)
                    .packageId(resolvedPackageId)
                    .packageName(resolvedPackageId != null
                            ? packageIdToName.getOrDefault(resolvedPackageId, FALLBACK_NAME)
                            : FALLBACK_NAME)
                    .productId(assignment.getProductId())
                    .productName(assignment.getProductId() != null
                            ? productIdToName.getOrDefault(assignment.getProductId(), FALLBACK_NAME)
                            : FALLBACK_NAME)
                    .subPackageId(assignment.getSubPackageId())
                    .subPackageName(assignment.getSubPackageName())
                    .assignedDate(assignment.getAssignedDate())
                    .expiryDate(assignment.getExpiryDate())
                    .status(PackageAssignmentStatusUtil.resolveDisplayStatus(
                            assignment.getAssignedDate(), assignment.getExpiryDate(),
                            assignment.getStatus(), today))
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

        Map<String, String> names = new HashMap<>();
        for (ProductPackage productPackage : productPackageRepository.findAllById(distinctIds)) {
            if (productPackage.getId() == null
                    || productPackage.getName() == null
                    || productPackage.getName().isBlank()) {
                continue;
            }
            names.put(productPackage.getId(), productPackage.getName());
        }

        List<String> unresolvedIds = distinctIds.stream()
                .filter(id -> !names.containsKey(id))
                .toList();
        if (!unresolvedIds.isEmpty()) {
            Map<String, String> bundleNames = userPackageRepositoryCustom.getBundleNamesByPackageIds(unresolvedIds);
            if (bundleNames != null) {
                bundleNames.forEach((id, name) -> {
                    if (id != null && name != null && !name.isBlank()) {
                        names.putIfAbsent(id, name);
                    }
                });
            }
        }
        return names;
    }

    private Map<String, String> resolveProductNames(List<UserSubPackage> assignments) {
        List<String> productIds = assignments.stream()
                .map(UserSubPackage::getProductId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (productIds.isEmpty()) {
            return Map.of();
        }
        Map<String, String> names = new HashMap<>();
        for (Product product : productRepository.findAllById(productIds)) {
            names.put(product.getId(), product.getProductName());
        }
        return names;
    }

    private Map<String, AspireUserBasicDto> resolveUsers(List<UserSubPackage> assignments) {
        List<String> userIds = assignments.stream()
                .map(UserSubPackage::getUserId)
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }

        Map<String, AspireUserBasicDto> usersById = new HashMap<>();
        List<AspireUserBasicDto> users = registrationServiceClient.getUsersByIds(userIds);
        if (users == null) {
            return usersById;
        }
        for (AspireUserBasicDto user : users) {
            if (user.getUserId() != null && !user.getUserId().isBlank()) {
                usersById.putIfAbsent(user.getUserId(), user);
            }
        }
        return usersById;
    }

    private static String resolveFullName(AspireUserBasicDto user, UserSubPackage assignment) {
        if (user != null && StringUtils.hasText(user.getFullName())) {
            return user.getFullName().trim();
        }
        if (StringUtils.hasText(assignment.getUserEmail())) {
            return assignment.getUserEmail();
        }
        return assignment.getUserId() != null ? assignment.getUserId() : FALLBACK_NAME;
    }

    private static String resolveEmail(AspireUserBasicDto user, UserSubPackage assignment) {
        if (user != null && StringUtils.hasText(user.getEmail())) {
            return user.getEmail();
        }
        return assignment.getUserEmail();
    }

    private static List<String> scopeClientAdminIds(ClientReportScope scope) {
        if (scope.clientAdminIds() != null && !scope.clientAdminIds().isEmpty()) {
            return scope.clientAdminIds();
        }
        if (StringUtils.hasText(scope.clientAdminId())) {
            return List.of(scope.clientAdminId());
        }
        return List.of();
    }

    private static byte[] buildCsv(List<LicenseAssignmentRowDto> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF');
        sb.append(String.join(",", EXPORT_HEADERS)).append('\n');
        for (LicenseAssignmentRowDto row : rows) {
            sb.append(escapeCsv(row.getFullName())).append(',');
            sb.append(escapeCsv(row.getEmail())).append(',');
            sb.append(escapeCsv(row.getDepartment())).append(',');
            sb.append(escapeCsv(row.getPackageName())).append(',');
            sb.append(escapeCsv(row.getProductName())).append(',');
            sb.append(row.getAssignedDate() != null ? row.getAssignedDate() : "").append(',');
            sb.append(row.getExpiryDate() != null ? row.getExpiryDate() : "").append(',');
            sb.append(escapeCsv(row.getStatus())).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] buildExcel(List<LicenseAssignmentRowDto> rows, boolean legacyXls) {
        try (Workbook workbook = legacyXls ? new HSSFWorkbook() : new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("License Assignments");
            Row header = sheet.createRow(0);
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                header.createCell(i).setCellValue(EXPORT_HEADERS[i]);
            }
            int rowIdx = 1;
            for (LicenseAssignmentRowDto row : rows) {
                Row excelRow = sheet.createRow(rowIdx++);
                excelRow.createCell(0).setCellValue(nullToEmpty(row.getFullName()));
                excelRow.createCell(1).setCellValue(nullToEmpty(row.getEmail()));
                excelRow.createCell(2).setCellValue(nullToEmpty(row.getDepartment()));
                excelRow.createCell(3).setCellValue(nullToEmpty(row.getPackageName()));
                excelRow.createCell(4).setCellValue(nullToEmpty(row.getProductName()));
                excelRow.createCell(5).setCellValue(row.getAssignedDate() != null ? row.getAssignedDate().toString() : "");
                excelRow.createCell(6).setCellValue(row.getExpiryDate() != null ? row.getExpiryDate().toString() : "");
                excelRow.createCell(7).setCellValue(nullToEmpty(row.getStatus()));
            }
            for (int i = 0; i < EXPORT_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to generate Excel export", e);
        }
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

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
