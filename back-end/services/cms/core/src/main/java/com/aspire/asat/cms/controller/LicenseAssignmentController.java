package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.license.LicenseAssignmentRowDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "License Assignments",
        description = "Paginated user license assignments for a client, with package, product, department, and status filters")
@RequestMapping(WebApiUrlConstants.LICENSE_ASSIGNMENTS_API)
public interface LicenseAssignmentController {

    @Operation(summary = "List user license assignments",
            description = "Returns paginated user–product assignment rows for License Assignments. "
                    + "CLIENT_ADMIN users are scoped to their organization; MSP users are scoped to clientAdminIds in context. "
                    + "Search matches user name or email via Registration. "
                    + "Status filter values: ACTIVE, EXPIRING, EXPIRING_SOON, EXPIRED, COMPLETE "
                    + "(display labels such as Active / Expiring Soon are also accepted). "
                    + "offset is a page index (skip = offset * pageSize).")
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<LicenseAssignmentRowDto>>>> listLicenseAssignments(
            @Parameter(description = "Client Admin ID to scope the list (optional for admin/MSP)")
            @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "MSP ID to scope the list (optional)")
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Search by user name or email")
            @RequestParam(required = false) String search,
            @Parameter(description = "Parent package ID (Gold / Silver dropdown)")
            @RequestParam(required = false) String packageId,
            @Parameter(description = "Product ID")
            @RequestParam(required = false) String productId,
            @Parameter(description = "Department name (exact, case-insensitive) or department document id")
            @RequestParam(required = false) String department,
            @Parameter(description = "Display status: ACTIVE, EXPIRING, EXPIRING_SOON, EXPIRED, or COMPLETE")
            @RequestParam(required = false) String status,
            @Parameter(description = "Assigned date from (yyyy-MM-dd; also accepts yyyy/MM/dd or ISO-8601)")
            @RequestParam(required = false) String fromDate,
            @Parameter(description = "Assigned date to (yyyy-MM-dd; also accepts yyyy/MM/dd or ISO-8601)")
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int pageSize);

    @Operation(summary = "Export user license assignments",
            description = "Downloads all matching assignment rows using the same filters as the list endpoint. "
                    + "format=csv returns CSV; format=xls or xlsx returns Excel. "
                    + "offset/pageSize are ignored; the full filtered set is exported (capped).")
    @GetMapping(WebApiUrlConstants.PATH_VAR_EXPORT)
    ResponseEntity<byte[]> exportLicenseAssignments(
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) String mspId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String packageId,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @Parameter(description = "Export format: csv, xls, or xlsx", example = "csv")
            @RequestParam(defaultValue = "csv") String format);
}
