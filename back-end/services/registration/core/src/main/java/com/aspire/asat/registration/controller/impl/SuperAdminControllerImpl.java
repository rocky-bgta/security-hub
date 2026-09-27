package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.SuperAdminController;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.superAdmin.response.LicenseHistoryPaginatedResponseDto;
import com.aspire.asat.registration.data.superAdmin.response.MspLicenseHistoryPaginatedResponseDto;
import com.aspire.asat.registration.service.SuperAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SuperAdminControllerImpl implements SuperAdminController {

    private final SuperAdminService superAdminService;

    @Override
    public ResponseEntity<ApiResponseDto<LicenseHistoryPaginatedResponseDto>> getLicenseHistory(
            String clientAdminId,
            String productId,
            String packageId,
            String countryId,
            String mspId,
            String search,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order) {

        try {
            log.info("Retrieving license history with filters - clientAdminId: {}, productId: {}, packageId: {}, countryId: {}, mspId: {}, search: {}, offset: {}, pageSize: {}, sortBy: {}, order: {}",
                    clientAdminId, productId, packageId, countryId, mspId, search, offset, pageSize, sortBy, order);

            LicenseHistoryPaginatedResponseDto response = superAdminService.getLicenseHistory(
                    clientAdminId, productId, packageId, countryId, mspId, search, offset, pageSize, sortBy, order);

            return ResponseEntity.ok(new ApiResponseDto<>("License history retrieved successfully", 200, response));

        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error retrieving license history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve license history: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<MspLicenseHistoryPaginatedResponseDto>> getMspLicenseHistory(
            String mspId,
            String productId,
            String packageId,
            String countryId,
            String search,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order) {

        try {
            log.info("Retrieving MSP license history with filters - mspId: {}, productId: {}, packageId: {}, countryId: {}, search: {}, offset: {}, pageSize: {}, sortBy: {}, order: {}",
                    mspId, productId, packageId, countryId, search, offset, pageSize, sortBy, order);

            MspLicenseHistoryPaginatedResponseDto response = superAdminService.getMspLicenseHistory(
                    mspId, productId, packageId, countryId, search, offset, pageSize, sortBy, order);

            return ResponseEntity.ok(new ApiResponseDto<>("MSP license history retrieved successfully", 200, response));

        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error retrieving MSP license history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve MSP license history: " + e.getMessage(), 500, null));
        }
    }
}

