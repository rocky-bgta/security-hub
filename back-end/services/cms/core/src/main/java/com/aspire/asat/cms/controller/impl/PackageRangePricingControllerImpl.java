package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.PackageRangePricingController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.packageRangePricing.BulkPackageRangePricingRequest;
import com.aspire.asat.cms.dto.packageRangePricing.PackageRangePricingRequest;
import com.aspire.asat.cms.dto.packageRangePricing.PackageRangePricingResponse;
import com.aspire.asat.cms.service.PackageRangePricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PackageRangePricingControllerImpl implements PackageRangePricingController {

    private final PackageRangePricingService packageRangePricingService;

    @Override
    public ResponseEntity<ApiResponseDto<PackageRangePricingResponse>> createPackageRangePricing(
            String packageId, @Valid PackageRangePricingRequest request) {
        PackageRangePricingResponse response = packageRangePricingService.createPackageRangePricing(request, packageId);
        ApiResponseDto<PackageRangePricingResponse> apiResponse = new ApiResponseDto<>(
                "Package range pricing created successfully", HttpStatus.CREATED.value(), response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<PackageRangePricingResponse>>> getPricingByPackageId(String packageId) {
        List<PackageRangePricingResponse> response = packageRangePricingService.getPricingByPackageId(packageId);
        ApiResponseDto<List<PackageRangePricingResponse>> apiResponse = new ApiResponseDto<>(
                "Package range pricing retrieved successfully", HttpStatus.OK.value(), response);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<PackageRangePricingResponse>> getPricingByPackageIdAndUserRangeId(
            String packageId, String userRangeId) {
        PackageRangePricingResponse response = packageRangePricingService.getPricingByPackageIdAndUserRangeId(packageId, userRangeId);
        ApiResponseDto<PackageRangePricingResponse> apiResponse = new ApiResponseDto<>(
                "Package range pricing retrieved successfully", HttpStatus.OK.value(), response);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<PackageRangePricingResponse>> updatePackageRangePricing(
            String id, @Valid PackageRangePricingRequest request) {
        PackageRangePricingResponse response = packageRangePricingService.updatePackageRangePricing(id, request);
        ApiResponseDto<PackageRangePricingResponse> apiResponse = new ApiResponseDto<>(
                "Package range pricing updated successfully", HttpStatus.OK.value(), response);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> deletePackageRangePricing(String id) {
        packageRangePricingService.deletePackageRangePricing(id);
        ApiResponseDto<String> apiResponse = new ApiResponseDto<>(
                "Package range pricing deleted successfully", HttpStatus.OK.value(), "Deleted pricing with id: " + id);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<PackageRangePricingResponse>>> bulkCreateOrUpdatePackageRangePricing(
            @Valid BulkPackageRangePricingRequest request) {
        List<PackageRangePricingResponse> response = packageRangePricingService.bulkCreateOrUpdatePackageRangePricing(request);
        ApiResponseDto<List<PackageRangePricingResponse>> apiResponse = new ApiResponseDto<>(
                "Package range pricing bulk operation completed successfully", HttpStatus.OK.value(), response);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}

