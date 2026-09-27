package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.enums.MspStatus;
import com.aspire.asat.registration.data.mspUser.request.MspBuyNowRequestDto;
import com.aspire.asat.registration.data.mspUser.request.MspOnboardingRequestDto;
import com.aspire.asat.registration.data.clientAdmin.response.OrganizationLicenseStatisticsResponseDto;
import com.aspire.asat.registration.data.mspUser.request.MspUpdateRequestDto;
import com.aspire.asat.registration.data.mspUser.request.MspStatusUpdateDto;
import com.aspire.asat.registration.data.mspUser.response.*;
import com.aspire.asat.registration.data.endUser.request.UserSuspendRequestDto;

import java.util.List;
import com.aspire.asat.registration.controller.MspUserController;
import com.aspire.asat.registration.data.clientAdmin.response.ClientDropdownDto;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.service.ClientAdminService;
import com.aspire.asat.registration.service.msp.MspUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MspUserControllerImpl implements MspUserController {

    private final MspUserService mspUserService;
    private final ClientAdminService clientAdminService;

    @Override
    public ResponseEntity<ApiResponseDto<MspOnboardingResponseDto>> onboardMspUser(MspOnboardingRequestDto requestDto) {
        MspOnboardingResponseDto response = mspUserService.processMspOnboarding(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("MSP user onboarded successfully", 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<MspBuyNowResponseDto>> buyNow(MspBuyNowRequestDto requestDto) {
        try {
            MspBuyNowResponseDto response = mspUserService.processMspBuyNow(requestDto);
            return ResponseEntity.ok(new ApiResponseDto<>("MSP buy-now flow completed successfully", 200, response));
        } catch (RegistrationServiceException e) {
            log.error("MSP buy-now failed: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>(e.getMessage(), 404, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Unexpected error during MSP buy-now: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to complete MSP buy-now flow: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<MspProductCatalogResponseDto>> getMspProductCatalog(
            String mspId, String search, String mspProductStatus, Integer offset, Integer pageSize) {
        try {
            MspProductCatalogResponseDto response = mspUserService.getMspProductCatalog(
                    mspId, search, mspProductStatus, offset, pageSize);
            return ResponseEntity.ok(new ApiResponseDto<>("MSP product catalog retrieved successfully", 200, response));
        } catch (RegistrationServiceException e) {
            log.error("Error retrieving MSP product catalog for {}: {}", mspId, e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>(e.getMessage(), 404, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving MSP product catalog for {}: {}", mspId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve MSP product catalog: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<MspAssignedProductResponseDto>>>> getAssignedMspProducts(
            String mspId, String search, String status, Integer offset, Integer pageSize, String sortBy, String order) {
        try {
            AllResponseDto<List<MspAssignedProductResponseDto>> response = mspUserService.getAssignedMspProducts(
                    mspId, search, status, offset, pageSize, sortBy, order);
            return ResponseEntity.ok(new ApiResponseDto<>("Assigned MSP products retrieved successfully", 200, response));
        } catch (RegistrationServiceException e) {
            log.error("Error retrieving assigned MSP products for {}: {}", mspId, e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>(e.getMessage(), 404, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving assigned MSP products for {}: {}", mspId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve assigned MSP products: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<MspProductDetailedDto>> getMspProductDetailById(String id) {
        try {
            log.info("Retrieving MSP product-package details for ID: {}", id);

            MspProductDetailedDto productDetails = mspUserService.getMspProductDetailById(id);

            return ResponseEntity.ok(new ApiResponseDto<>(
                    "MSP product-package details retrieved successfully", 200, productDetails));

        } catch (RegistrationServiceException e) {
            log.error("MSP product-package not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error retrieving MSP product-package details for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(
                            "Failed to retrieve MSP product-package details: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<MspListResponseDto>>>> getAllMsp(Integer offset, Integer pageSize, String search, com.aspire.asat.registration.data.enums.MspStatus status) {
        AllResponseDto<List<MspListResponseDto>> response = mspUserService.getAllMsp(offset, pageSize, search, status);
        return ResponseEntity.ok(new ApiResponseDto<>("MSPs retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<MspDetailsResponseDto>> getMspDetails(String id) {
        MspDetailsResponseDto response = mspUserService.getMspDetails(id);
        return ResponseEntity.ok(new ApiResponseDto<>("MSP details retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<MspViewDetailsResponseDto>> getMspViewDetails(String id) {
        MspViewDetailsResponseDto response = mspUserService.getMspViewDetails(id);
        return ResponseEntity.ok(new ApiResponseDto<>("MSP details retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<OrganizationLicenseStatisticsResponseDto>> getLicenseStatistics(String mspId) {
        try {
            log.info("Retrieving organization license statistics for MSP: {}", mspId);

            OrganizationLicenseStatisticsResponseDto statistics = mspUserService.getLicenseStatistics(mspId);

            return ResponseEntity.ok(new ApiResponseDto<>("Organization license statistics retrieved successfully", 200, statistics));

        } catch (RegistrationServiceException e) {
            log.error("MSP not found with ID: {}", mspId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error retrieving organization license statistics for MSP: {}", mspId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve organization license statistics: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<MspOnboardedListResponseDto>>>> getOnboardedMsps(
            Integer offset, Integer pageSize, String search,
            MspStatus status, Integer minLicenses, Integer maxLicenses) {

        log.info("Getting onboarded MSPs - offset: {}, pageSize: {}, search: {}, status: {}, minLicenses: {}, maxLicenses: {}",
                offset, pageSize, search, status, minLicenses, maxLicenses);

        AllResponseDto<List<MspOnboardedListResponseDto>> response =
                mspUserService.getOnboardedMsps(offset, pageSize, search, status, minLicenses, maxLicenses);

        String message = response.getItems().isEmpty()
                ? "No onboarded MSPs found"
                : "Onboarded MSPs retrieved successfully";

        return ResponseEntity.ok(new ApiResponseDto<>(message, 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> updateMspStatus(String id, MspStatus status) {
        log.info("Updating MSP status - ID: {}, New Status: {}", id, status);

        try {
            mspUserService.updateMspStatus(id, status);

            String message = String.format("MSP status updated to %s successfully", status);
            return ResponseEntity.ok(new ApiResponseDto<>(message, 200, null));

        } catch (RegistrationServiceException e) {
            log.error("MSP not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error updating MSP status for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to update MSP status: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<MspUpdateResponseDto>> updateMsp(String id, MspUpdateRequestDto requestDto) {
        log.info("Received request to update MSP: {}", id);

        try {
            MspUpdateResponseDto response = mspUserService.updateMsp(id, requestDto);
            return ResponseEntity.ok(new ApiResponseDto<>("MSP updated successfully", 200, response));

        } catch (RegistrationServiceException e) {
            log.error("MSP not found or validation error for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error updating MSP with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to update MSP: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> updateMspUserStatus(String userId, MspStatusUpdateDto statusUpdateDto) {
        try {
            log.info("Updating MSP user status for userId: {} to status: {}", userId, statusUpdateDto.getStatus());
            mspUserService.updateMspUserStatus(userId, statusUpdateDto.getStatus());
            return ResponseEntity.ok(new ApiResponseDto<>("MSP user status updated successfully", 200, null));
        } catch (RegistrationServiceException e) {
            log.error("Error updating MSP user status for userId: {}: {}", userId, e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>(e.getMessage(), 404, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (IllegalArgumentException e) {
            log.error("Invalid status update request for userId: {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Unexpected error updating MSP user status for userId: {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to update MSP user status: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> suspendMspUser(String userId, UserSuspendRequestDto requestDto) {
        try {
            log.info("Suspending/activating MSP user for userId: {} to status: {}", userId, requestDto.getStatus());
            mspUserService.suspendMspUser(userId, requestDto);
            
            String message = "SUSPEND".equalsIgnoreCase(requestDto.getStatus()) 
                    ? "MSP user suspended successfully" 
                    : "MSP user activated successfully";
            return ResponseEntity.ok(new ApiResponseDto<>(message, 200, null));
        } catch (RegistrationServiceException e) {
            log.error("Error suspending/activating MSP user for userId: {}: {}", userId, e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>(e.getMessage(), 404, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (IllegalArgumentException e) {
            log.error("Invalid status update request for userId: {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Unexpected error suspending/activating MSP user for userId: {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to suspend/activate MSP user: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> activateMspLicense(String mspId, String invoiceId) {
        try {
            mspUserService.activateLicense(mspId, invoiceId);
            return ResponseEntity.ok(new ApiResponseDto<>("MSP license activated successfully", 200, null));
        } catch (Exception e) {
            log.error("Error activating license for MSP {}: {}", mspId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to activate MSP license", 500, null));
        }
    }
    @Override
    public ResponseEntity<ApiResponseDto<List<ClientDropdownDto>>> getMspClientsDropdown(
            String mspId, String country, String state, String search) {
        try {
            List<ClientDropdownDto> clients = clientAdminService.getClientsDropdownForMsp(mspId, country, state, search);
            return ResponseEntity.ok(new ApiResponseDto<>("Clients dropdown retrieved successfully", 200, clients));
        } catch (com.aspire.asat.registration.exception.RegistationValidationException e) {
            log.error("Invalid clients dropdown request for MSP {}: {}", mspId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (RegistrationServiceException e) {
            log.error("Access denied or error retrieving clients dropdown for MSP {}: {}", mspId, e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("Access denied")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponseDto<>(e.getMessage(), 403, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Unexpected error retrieving clients dropdown for MSP {}: {}", mspId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve clients dropdown: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<AssignedClientDto>>>> getAssignedClientsForMsp(
            String mspId, Integer offset, Integer pageSize) {
        try {
            log.info("Getting assigned clients for MSP: {} with offset: {}, pageSize: {}", mspId, offset, pageSize);

            List<com.aspire.asat.registration.data.mspUser.response.AssignedClientDto> clients =
                    mspUserService.getAssignedClientsForMsp(mspId, offset, pageSize);
            long total = mspUserService.countAssignedClientsForMsp(mspId);

            AllResponseDto<List<com.aspire.asat.registration.data.mspUser.response.AssignedClientDto>> response =
                    new AllResponseDto<>(offset != null ? offset : 0, pageSize != null ? pageSize : 10, total, clients);

            return ResponseEntity.ok(new ApiResponseDto<>("Assigned clients retrieved successfully", 200, response));
        } catch (RegistrationServiceException e) {
            log.error("Error getting assigned clients for MSP: {}: {}", mspId, e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>(e.getMessage(), 404, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Unexpected error getting assigned clients for MSP: {}: {}", mspId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to get assigned clients: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<com.aspire.asat.registration.data.cms.response.CmsTopicMinimalDto>>>> getClientProductTopicsByMspId(
            String mspId, com.aspire.asat.registration.data.mspUser.request.MspClientProductTopicsRequestDto request) {
        try {
            log.info("Getting MSP-product topics for MSP: {}", mspId);

            AllResponseDto<List<com.aspire.asat.registration.data.cms.response.CmsTopicMinimalDto>> response =
                    mspUserService.getClientProductTopicsByMspId(mspId, request);

            return ResponseEntity.ok(new ApiResponseDto<>("Topics retrieved successfully", 200, response));
        } catch (RegistrationServiceException e) {
            log.error("Error getting client-product topics for MSP {}: {}", mspId, e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>(e.getMessage(), 404, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Unexpected error getting client-product topics for MSP {}: {}", mspId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve topics: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<com.aspire.asat.registration.data.mspUser.response.MspListWithLicenseResponseDto>>>> getMspListWithLicenses(
            Integer offset, Integer pageSize, String search, MspStatus status, String country, String mspTier) {
        try {
            log.info("Getting MSP list with licenses - offset: {}, pageSize: {}, search: {}, status: {}, country: {}, mspTier: {}",
                    offset, pageSize, search, status, country, mspTier);

            AllResponseDto<List<com.aspire.asat.registration.data.mspUser.response.MspListWithLicenseResponseDto>> response =
                    mspUserService.getMspListWithLicenses(offset, pageSize, search, status, mspTier, country);

            String message = response.getItems().isEmpty()
                    ? "No MSPs found"
                    : "MSP list retrieved successfully";

            return ResponseEntity.ok(new ApiResponseDto<>(message, 200, response));
        } catch (Exception e) {
            log.error("Error getting MSP list with licenses: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to get MSP list: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<com.aspire.asat.registration.data.mspUser.response.MspActiveLicenseSummaryResponseDto>> getActiveLicenseSummary() {
        try {
            log.info("Getting active license summary");

            com.aspire.asat.registration.data.mspUser.response.MspActiveLicenseSummaryResponseDto response =
                    mspUserService.getActiveLicenseSummary();

            return ResponseEntity.ok(new ApiResponseDto<>("Active license summary retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting active license summary: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to get active license summary: " + e.getMessage(), 500, null));
        }
    }


}
