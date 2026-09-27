package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.packageRangePricing.BulkPackageRangePricingRequest;
import com.aspire.asat.cms.dto.packageRangePricing.PackageRangePricingRequest;
import com.aspire.asat.cms.dto.packageRangePricing.PackageRangePricingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequestMapping(value = WebApiUrlConstants.PACKAGE_RANGE_PRICING_API, produces = "application/json")
@Tag(name = "Package Range Pricing Management", description = "APIs for managing package range-based pricing")
public interface PackageRangePricingController {

    @PostMapping(consumes = "application/json")
    @Operation(summary = "Create package range pricing", description = "Creates range-based pricing for a package.")
    ResponseEntity<ApiResponseDto<PackageRangePricingResponse>> createPackageRangePricing(
            @RequestParam("packageId") String packageId,
            @Valid @RequestBody PackageRangePricingRequest request);

    @GetMapping("/package/{packageId}")
    @Operation(summary = "Get pricing by package ID", description = "Retrieves all range pricing for a specific package.")
    ResponseEntity<ApiResponseDto<List<PackageRangePricingResponse>>> getPricingByPackageId(
            @PathVariable String packageId);

    @GetMapping("/package/{packageId}/range")
    @Operation(summary = "Get pricing by package ID", description = "Retrieves range pricing for a specific package. Optionally filter by user range ID.")
    public ResponseEntity<ApiResponseDto<PackageRangePricingResponse>> getPricingByPackageIdAndUserRangeId(
            @PathVariable String packageId,
            @RequestParam(value = "rangeId", required = false) String userRangeId);

    @PutMapping(WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Update package range pricing", description = "Updates an existing package range pricing.")
    ResponseEntity<ApiResponseDto<PackageRangePricingResponse>> updatePackageRangePricing(
            @PathVariable String id,
            @Valid @RequestBody PackageRangePricingRequest request);

    @DeleteMapping(WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Delete package range pricing", description = "Soft deletes a package range pricing.")
    ResponseEntity<ApiResponseDto<String>> deletePackageRangePricing(@PathVariable String id);

    @PostMapping(value = "/bulk", consumes = "application/json")
    @Operation(summary = "Bulk create/update package range pricing", description = "Creates or updates multiple range pricing entries for a package.")
    ResponseEntity<ApiResponseDto<List<PackageRangePricingResponse>>> bulkCreateOrUpdatePackageRangePricing(
            @Valid @RequestBody BulkPackageRangePricingRequest request);
}

