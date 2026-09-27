package com.aspire.asat.cms.controller.reports;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.reports.PackageAssignmentReportDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Package Assignment Report",
        description = "Distribution of product packages across users with summary metrics and assignment log")
@RequestMapping(WebApiUrlConstants.PACKAGE_ASSIGNMENT_REPORT_API)
public interface PackageAssignmentReportController {

    @Operation(summary = "Get package assignment report",
            description = "Returns summary cards and paginated assignment log. "
                    + "CLIENT_ADMIN users are scoped to their organization; MSP users are scoped to clientAdminIds in context. "
                    + "Expiring Soon uses an 80% elapsed-progress rule between assignedDate and expiryDate. "
                    + "Status filter values: ACTIVE, EXPIRING, EXPIRED, COMPLETE.")
    @GetMapping
    ResponseEntity<ApiResponseDto<PackageAssignmentReportDTO>> getPackageAssignmentReport(
            @Parameter(description = "Client Admin ID to scope the report (optional for admin/MSP)")
            @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "MSP ID to scope the report (optional)")
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Search term applied to user email and sub-package name")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by status tab: ACTIVE, EXPIRING, EXPIRED, or COMPLETE (user_subpackages.status=COMPLETED)")
            @RequestParam(required = false) String status,
            @Parameter(description = "Assigned date from (yyyy-MM-dd, e.g. 2026-06-26; also accepts yyyy/MM/dd or ISO-8601)")
            @RequestParam(required = false) String fromDate,
            @Parameter(description = "Assigned date to (yyyy-MM-dd, e.g. 2026-06-26; also accepts yyyy/MM/dd or ISO-8601)")
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int pageSize);

    @Operation(summary = "Export package assignment report as CSV",
            description = "Downloads the assignment log as CSV using the same filters as the report endpoint, "
                    + "including status=COMPLETE for the Complete tab. "
                    + "Columns: User, Package, Sub Package, Assigned, Expiry, Status, Assigned By. "
                    + "User names are resolved from AspireUser via Registration in batches of page size.")
    @GetMapping("/export")
    ResponseEntity<Resource> exportPackageAssignmentReport(
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate);
}
