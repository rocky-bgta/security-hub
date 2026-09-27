package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.controller.MspProductController;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspPackageSimpleResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspProductSimpleResponseDto;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.service.msp.MspUserService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MspProductControllerImpl implements MspProductController {

    private final MspUserService mspUserService;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public ResponseEntity<ApiResponseDto<List<MspProductSimpleResponseDto>>> getUniqueProductsByMspId(String mspId) {
        try {
            log.info("Received request to get unique products for mspId: {}", mspId);

            List<MspProductSimpleResponseDto> products = mspUserService.getUniqueProductsByMspId(mspId);

            return ResponseEntity.ok(new ApiResponseDto<>(
                    "Products retrieved successfully",
                    HttpStatus.OK.value(),
                    products
            ));
        } catch (RegistrationServiceException e) {
            log.error("Error retrieving unique products for mspId {}: {}", mspId, e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>(e.getMessage(), 404, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving unique products for mspId {}: {}", mspId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(
                            "Failed to retrieve products: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<MspPackageSimpleResponseDto>>> getPackagesByMspIdAndProductId(
            String mspId, String productId) {
        try {
            String resolvedMspId = resolveMspId(mspId);

            log.info("Received request to get packages for mspId: {} and productId: {}",
                    resolvedMspId, productId);

            List<MspPackageSimpleResponseDto> packages =
                    mspUserService.getPackagesByMspIdAndProductId(resolvedMspId, productId);

            return ResponseEntity.ok(new ApiResponseDto<>(
                    "Packages retrieved successfully",
                    HttpStatus.OK.value(),
                    packages
            ));
        } catch (RegistrationServiceException e) {
            log.error("Error retrieving packages for productId {}: {}", productId, e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>(e.getMessage(), 404, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving packages for productId {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(
                            "Failed to retrieve packages: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<String>>> getPurchasedPackageIdsByMspIdAndProductId(
            String mspId, String productId) {
        try {
            String resolvedMspId = resolveMspId(mspId);

            log.info("Received request to get purchased package IDs for mspId: {} and productId: {}",
                    resolvedMspId, productId);

            List<String> packageIds =
                    mspUserService.getPurchasedPackageIdsByMspIdAndProductId(resolvedMspId, productId);

            return ResponseEntity.ok(new ApiResponseDto<>(
                    "Purchased package IDs retrieved successfully",
                    HttpStatus.OK.value(),
                    packageIds
            ));
        } catch (RegistrationServiceException e) {
            log.error("Error retrieving purchased package IDs for productId {}: {}", productId, e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>(e.getMessage(), 404, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving purchased package IDs for productId {}: {}",
                    productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(
                            "Failed to retrieve purchased package IDs: " + e.getMessage(), 500, null));
        }
    }

    /**
     * For MSP callers, prefer context mspId when already set; otherwise use context userId.
     * For other callers, use the mspId query param.
     */
    private String resolveMspId(String requestMspId) {
        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        if (userContext != null && UserType.MSP.name().equalsIgnoreCase(userContext.getUserType())) {
            return userContext.getUserId();
        }

        if (requestMspId == null || requestMspId.isBlank()) {
            throw new RegistrationServiceException("mspId is required");
        }
        return requestMspId;
    }
}
