package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.clientAdmin.request.BuyNowRequestDto;
import com.aspire.asat.registration.data.clientAdmin.request.ClientOnboardingRequestDto;
import com.aspire.asat.registration.data.clientAdmin.request.ClientProductAssignment;
import com.aspire.asat.registration.controller.ClientAdminController;
import com.aspire.asat.registration.data.clientAdmin.response.*;
import com.aspire.asat.registration.data.clientAdmin.request.ClientAdminListRequestDto;
import com.aspire.asat.registration.data.clientAdmin.request.ClientAdminStatusUpdateDto;
import com.aspire.asat.registration.data.clientAdmin.request.ClientAdminUpdateRequestDto;
import com.aspire.asat.registration.data.clientAdmin.request.ClientLicenseDeactivateRequestDto;
import com.aspire.asat.registration.data.clientAdmin.request.AdminStatus;
import com.aspire.asat.common.enums.ActivityType;
import com.aspire.asat.registration.data.endUser.request.UserSuspendRequestDto;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.service.ClientAdminService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ClientAdminControllerImpl implements ClientAdminController {

    private final ClientAdminService clientAdminService;
    private final UserCurrentContextService userCurrentContextService;
    private final MessageService messageService;

    @Override
    @LogActivity(
            activityType = ActivityType.CLIENT_CREATED,
            description = "Created client admin: #{#requestDto.organization.organizationName}"
    )
    public ResponseEntity<ApiResponseDto<ClientOnboardingResponseDto>> onboardClientAdmin(ClientOnboardingRequestDto requestDto) {
        ClientOnboardingResponseDto response = clientAdminService.processClientOnboarding(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Client admin onboarded successfully", 201, response));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.CLIENT_UPDATED,
            description = "Buy now flow for client admin: #{#requestDto.clientAdminId}",
            clientAdminIdExpression = "#{#requestDto.clientAdminId}"
    )
    public ResponseEntity<ApiResponseDto<BuyNowResponseDto>> buyNow(BuyNowRequestDto requestDto) {
        try {
            log.info("Processing buy now flow for client admin: {}", requestDto.getClientAdminId());
            BuyNowResponseDto response = clientAdminService.processBuyNow(requestDto);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.BILLING_PURCHASE_COMPLETED), 200, response));
        } catch (RegistrationServiceException e) {
            log.error("Error in buy now flow for client admin {}: {}", requestDto.getClientAdminId(), e.getMessage());
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>(e.getMessage(), 404, null));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error in buy now flow for client admin {}: {}", requestDto.getClientAdminId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to process buy now flow: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<EmailTemplateValidationResponseDto>> validateEmailTemplate(String templateBody) {
        EmailTemplateValidationResponseDto result = clientAdminService.validateTemplate(templateBody);
        return ResponseEntity.ok(new ApiResponseDto<>("Validation completed", 200, result));
    }


    @Override
    public ResponseEntity<ApiResponseDto<UsernameValidationResponseDto>> checkUsernameExists(String email) {
        boolean exists = clientAdminService.doesUsernameExist(email);
        return ResponseEntity.ok(new ApiResponseDto<>("Checked username availability", 200,
                new UsernameValidationResponseDto(email, exists)));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.LICENSE_ALLOCATED,
            description = "Activated license for client admin: #{#clientId}",
            clientAdminIdExpression = "#{#clientId}"
    )
    public ResponseEntity<ApiResponseDto<Void>> activateClientLicense(String clientId) {
        try {
            clientAdminService.activateLicense(clientId);
            return ResponseEntity.ok(new ApiResponseDto<>("Client license activated successfully", 200, null));
        } catch (Exception e) {
            log.error("Error activating license for client admin {}: {}", clientId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to activate client license", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deactivateClientLicense(
            String clientId, ClientLicenseDeactivateRequestDto requestDto) {
        try {
            List<String> productIds = requestDto != null ? requestDto.getClientProductIds() : null;
            clientAdminService.deactivateLicense(clientId, productIds);
            return ResponseEntity.ok(new ApiResponseDto<>("Client license deactivated successfully", 200, null));
        } catch (Exception e) {
            log.error("Error deactivating license for client admin {}: {}", clientId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to deactivate client license", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> expireClientLicenseDueToUnpaidInvoice(
            String clientId, ClientLicenseDeactivateRequestDto requestDto) {
        try {
            List<String> productIds = requestDto != null ? requestDto.getClientProductIds() : null;
            clientAdminService.expireLicenseDueToUnpaidInvoice(clientId, productIds);
            return ResponseEntity.ok(new ApiResponseDto<>("Client license expired successfully", 200, null));
        } catch (Exception e) {
            log.error("Error expiring license for client admin {}: {}", clientId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to expire client license", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientProductIdListResponseDto>> getClientProductIds(String clientAdminId, String productId) {
        List<String> endUserIds = clientAdminService.findEndUsersByClientAdminId(clientAdminId);
        System.out.println( "***** End User IDs: " + endUserIds );
        List<ClientProductDTO> clientProductList = clientAdminService.findAllClientProductsByClientAdminId(clientAdminId, productId);

        return ResponseEntity.ok(new ApiResponseDto<>(
                "Product IDs fetched successfully",
                200,
                new ClientProductIdListResponseDto(endUserIds, clientProductList)
        ));
    }

    @Override
    @LogActivity(
            activityType = ActivityType.LICENSE_ALLOCATED,
            description = "Updated used license count for client admin: #{#clientAdminId}, product: #{#productId}, count: #{#usedLicenseCount}",
            clientAdminIdExpression = "#{#clientAdminId}"
    )
    public ResponseEntity<ApiResponseDto<Void>> updateUsedLicenseCount(String clientAdminId, String productId, int usedLicenseCount) {
        try {
            clientAdminService.updateUsedLicenseCount(clientAdminId, productId, usedLicenseCount);
            return ResponseEntity.ok(new ApiResponseDto<>("Used license count updated successfully", 200, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error updating used license count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Internal server error while updating license count", 500, null));
        }
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_ASSIGNED,
            description = "Reassigned products to client admin: #{#assignmentRequest.clientAdminId}",
            clientAdminIdExpression = "#{#assignmentRequest.clientAdminId}"
    )
    public ResponseEntity<ApiResponseDto<ClientProductReassignmentResponseDto>> reassignProductsToClient(ClientProductAssignment assignmentRequest) {
        try {
            log.info("Processing product reassignment for client admin: {}", assignmentRequest.getClientAdminId());
            ClientProductReassignmentResponseDto response = clientAdminService.reassignProductsToClient(assignmentRequest);
            return ResponseEntity.ok(new ApiResponseDto<>("Products reassigned successfully", 200, response));
        } catch (Exception e) {
            log.error("Error reassigning products to client admin {}: {}", assignmentRequest.getClientAdminId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to reassign products: " + e.getMessage(), 500, null));
        }

    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientAdminListResponseDto>> listClientAdminsWithProducts(
            String search, String mspId, Integer offset, Integer pageSize, AdminStatus status, String createdAt,
            String country, String state) {

        try {
            log.info("Listing client admins with products - search: {}, mspId: {}, offset: {}, pageSize: {}, status: {}, createdAt: {}, country: {}, state: {}",
                    search, mspId, offset, pageSize, status, createdAt, country, state);

            // Parse creation date
            Instant createdInstant = null;
            if (createdAt != null && !createdAt.trim().isEmpty()) {
                try {
                    createdInstant = Instant.parse(createdAt);
                } catch (Exception e) {
                    log.warn("Invalid date format: {}", createdAt);
                    return ResponseEntity.badRequest()
                            .body(new ApiResponseDto<>("Invalid date format. Use ISO-8601 format (e.g., 2024-01-15T10:30:00Z)", 400, null));
                }
            }

            // Build request DTO
            ClientAdminListRequestDto requestDto = ClientAdminListRequestDto.builder()
                    .search(search)
                    .mspId(mspId)
                    .offset(offset)
                    .pageSize(pageSize)
                    .status(status)
                    .createdAt(createdInstant)
                    .country(country)
                    .state(state)
                    .build();

            // Call service
            ClientAdminListResponseDto response = clientAdminService.listClientAdminsWithProducts(requestDto);

            return ResponseEntity.ok(new ApiResponseDto<>("Client admins retrieved successfully", 200, response));

        } catch (Exception e) {
            log.error("Error listing client admins with products: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve client admins: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientAdminDetailedResponseDto>> getClientAdminDetailedById(String clientAdminId) {
        try {
            log.info("Retrieving detailed client admin information for ID: {}", clientAdminId);
            
            ClientAdminDetailedResponseDto clientAdmin = clientAdminService.getClientAdminDetailedById(clientAdminId);
            
            return ResponseEntity.ok(new ApiResponseDto<>("Detailed client admin information retrieved successfully", 200, clientAdmin));
            
        } catch (RegistrationServiceException e) {
            log.error("Client admin not found with ID: {}", clientAdminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error retrieving detailed client admin information for ID: {}", clientAdminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve detailed client admin information: " + e.getMessage(), 500, null));
        }
    }

    @Override
    @LogActivity(
            activityType = ActivityType.CLIENT_UPDATED,
            description = "Updated client admin: #{#clientAdminId}",
            newValueExpression = "#{#updateRequestDto.organizationName != null ? #updateRequestDto.organizationName : #clientAdminId}",
            clientAdminIdExpression = "#{#clientAdminId}"
    )
    public ResponseEntity<ApiResponseDto<ClientAdminWithProductsResponseDto>> updateClientAdminById(String clientAdminId, ClientAdminUpdateRequestDto updateRequestDto) {
        try {
            log.info("Updating client admin details for ID: {}", clientAdminId);
            
            ClientAdminWithProductsResponseDto updatedClientAdmin = clientAdminService.updateClientAdminById(clientAdminId, updateRequestDto);
            
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.PROFILE_UPDATED), 200, updatedClientAdmin));
            
        } catch (RegistrationServiceException e) {
            log.error("Client admin not found with ID: {}", clientAdminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error updating client admin details for ID: {}", clientAdminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to update client admin details: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientProductsPaginatedResponseDto>> getAssignedClientProducts(
            String clientAdminId, String search, Integer offset, Integer pageSize, String sortBy, String order) {
        try {
            log.info("Retrieving assigned client products for client admin: {} with search: {}, offset: {}, pageSize: {}, sortBy: {}, order: {}", 
                    clientAdminId, search, offset, pageSize, sortBy, order);

            ClientProductsPaginatedResponseDto response = clientAdminService.getAssignedClientProducts(
                    clientAdminId, search, offset, pageSize, sortBy, order);

            return ResponseEntity.ok(new ApiResponseDto<>("Client products retrieved successfully", 200, response));

        } catch (RegistrationServiceException e) {
            log.error("Client admin not found with ID: {}", clientAdminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error retrieving assigned client products for client admin: {}", clientAdminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve assigned client products: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientProductsPaginatedResponseDto>> getAssignedActiveAndPendingClientProducts(
            String clientAdminId, String search, Integer offset, Integer pageSize, String sortBy, String order) {
        try {
            log.info("Retrieving ACTIVE and PENDING client products for client admin: {} with search: {}, offset: {}, pageSize: {}, sortBy: {}, order: {}",
                    clientAdminId, search, offset, pageSize, sortBy, order);

            ClientProductsPaginatedResponseDto response = clientAdminService.getAssignedActiveAndPendingClientProducts(
                    clientAdminId, search, offset, pageSize, sortBy, order);

            return ResponseEntity.ok(new ApiResponseDto<>("Client products retrieved successfully", 200, response));

        } catch (RegistrationServiceException e) {
            log.error("Client admin not found with ID: {}", clientAdminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error retrieving ACTIVE and PENDING client products for client admin: {}", clientAdminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve assigned client products: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<LicenseStatisticsResponseDto>> getLicenseStatistics(String productId) {
        String clientAdminId = null;
        try {
            CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
            clientAdminId = userContext.getUserId();


            log.info("Retrieving license statistics for client admin: {}, productId: {}", clientAdminId, productId);

            LicenseStatisticsResponseDto statistics = clientAdminService.getLicenseStatistics(clientAdminId, productId);

            return ResponseEntity.ok(new ApiResponseDto<>("License statistics retrieved successfully", 200, statistics));

        } catch (RegistrationServiceException e) {
            log.error("Client admin not found with ID: {}", clientAdminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error retrieving license statistics for client admin: {}", clientAdminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve license statistics: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<LicenseOverviewSummaryDto>> getLicenseOverviewSummary(String clientAdminId) {
        String resolvedClientAdminId = (clientAdminId != null && !clientAdminId.isBlank())
                ? clientAdminId.trim()
                : null;
        try {
            if (resolvedClientAdminId == null) {
                CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
                resolvedClientAdminId = firstNonBlank(userContext.getClientAdminId(), userContext.getUserId());
            }
            if (resolvedClientAdminId == null || resolvedClientAdminId.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponseDto<>("clientAdminId is required", 400, null));
            }
            LicenseOverviewSummaryDto summary = clientAdminService.getLicenseOverviewSummary(resolvedClientAdminId);
            return ResponseEntity.ok(new ApiResponseDto<>("License overview summary retrieved successfully", 200, summary));
        } catch (RegistrationServiceException e) {
            log.error("Client admin not found with ID: {}", resolvedClientAdminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error retrieving license overview summary for client admin: {}", resolvedClientAdminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve license overview summary: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientProductDetailedDto>> getProductPackageDetailById(String id) {
        try {
            log.info("Retrieving product-package details for ID: {}", id);

            ClientProductDetailedDto productDetails = clientAdminService.getProductPackageDetailById(id);

            return ResponseEntity.ok(new ApiResponseDto<>("Product-package details retrieved successfully", 200, productDetails));

        } catch (RegistrationServiceException e) {
            log.error("Product-package not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error retrieving product-package details for ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve product-package details: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> updateUsedLicenseCountByClientProductId(
            String id, int usedLicenseCount) {
        try {
            clientAdminService.updateUsedLicenseCountByClientProductId(id, usedLicenseCount);
            return ResponseEntity.ok(new ApiResponseDto<>(
                    "Used license count updated successfully", 200, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error updating used license count for clientProductId={}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(
                            "Internal server error while updating license count", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<MspIdResponseDto>> getMspIdByClientAdminId(String clientAdminId) {
        try {
            log.info("Retrieving MSP ID for client admin: {}", clientAdminId);

            String mspId = clientAdminService.getMspIdByClientAdminId(clientAdminId);

            MspIdResponseDto response = MspIdResponseDto.builder()
                    .clientAdminId(clientAdminId)
                    .mspId(mspId)
                    .build();

            return ResponseEntity.ok(new ApiResponseDto<>("MSP ID retrieved successfully", 200, response));

        } catch (RegistrationServiceException e) {
            log.error("Client admin not found or MSP ID not available for client admin: {}", clientAdminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error retrieving MSP ID for client admin: {}", clientAdminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve MSP ID: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<ClientMspListItemDto>>> getClientsOrMspsByCountry(String country, boolean isClient) {
        try {
            log.info("Retrieving clients/MSPs by country: {}, isClient: {}", country, isClient);

            List<ClientMspListItemDto> items = clientAdminService.getClientsOrMspsByCountry(country, isClient);

            return ResponseEntity.ok(new ApiResponseDto<>("Clients/MSPs retrieved successfully", 200, items));

        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error retrieving clients/MSPs by country: {}", country, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve clients/MSPs: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<OrganizationLicenseStatisticsResponseDto>> getOrganizationLicenseStatistics(String clientAdminId) {
        try {
            log.info("Retrieving organization license statistics for client admin: {}", clientAdminId);

            OrganizationLicenseStatisticsResponseDto statistics = clientAdminService.getOrganizationLicenseStatistics(clientAdminId);

            return ResponseEntity.ok(new ApiResponseDto<>("Organization license statistics retrieved successfully", 200, statistics));

        } catch (RegistrationServiceException e) {
            log.error("Client admin not found with ID: {}", clientAdminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error retrieving organization license statistics for client admin: {}", clientAdminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve organization license statistics: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<ClientDropdownDto>>> getClientsByMspId(String mspId) {
        try {
            log.info("Retrieving clients dropdown for mspId: {}", mspId);

            List<ClientDropdownDto> clients = clientAdminService.getClientsByMspId(mspId);

            return ResponseEntity.ok(new ApiResponseDto<>("Clients retrieved successfully", 200, clients));

        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error retrieving clients dropdown for mspId: {}", mspId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve clients: " + e.getMessage(), 500, null));
        }
    }

    @Override
    @LogActivity(
            activityType = ActivityType.CLIENT_STATUS_CHANGED,
            description = "Updated client admin status: #{#clientAdminId} to #{#statusUpdateDto.status}",
            oldValueExpression = "#{#clientAdminId}",
            newValueExpression = "#{#statusUpdateDto.status != null ? #statusUpdateDto.status.name() : 'N/A'}",
            clientAdminIdExpression = "#{#clientAdminId}"
    )
    public ResponseEntity<ApiResponseDto<Void>> updateClientAdminStatus(String clientAdminId, ClientAdminStatusUpdateDto statusUpdateDto) {
        try {
            log.info("Updating client admin status for ID: {} to status: {}", clientAdminId, statusUpdateDto.getStatus());

            clientAdminService.updateClientAdminStatus(clientAdminId, statusUpdateDto.getStatus());

            return ResponseEntity.ok(new ApiResponseDto<>("Client admin status updated successfully", 200, null));

        } catch (RegistrationServiceException e) {
            log.error("Client admin not found with ID: {}", clientAdminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error updating client admin status for ID: {}", clientAdminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to update client admin status: " + e.getMessage(), 500, null));
        }
    }

    @Override
    @LogActivity(
            activityType = ActivityType.USER_STATUS_CHANGED,
            description = "Updated AspireUser status: #{#userId} to #{#requestDto.status}",
            oldValueExpression = "#{#userId}",
            newValueExpression = "#{#requestDto.status != null ? #requestDto.status : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<Void>> updateAspireUserStatus(String userId, UserSuspendRequestDto requestDto) {
        try {
            log.info("Updating AspireUser status for userId: {} to status: {}", userId, requestDto.getStatus());

            clientAdminService.updateAspireUserStatus(userId, requestDto);

            String message = "SUSPEND".equalsIgnoreCase(requestDto.getStatus()) 
                    ? "User suspended successfully" 
                    : "User activated successfully";
            return ResponseEntity.ok(new ApiResponseDto<>(message, 200, null));

        } catch (RegistrationServiceException e) {
            log.error("Error updating AspireUser status for userId: {}: {}", userId, e.getMessage());
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
            log.error("Unexpected error updating AspireUser status for userId: {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to update user status: " + e.getMessage(), 500, null));
        }
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return null;
    }

}
