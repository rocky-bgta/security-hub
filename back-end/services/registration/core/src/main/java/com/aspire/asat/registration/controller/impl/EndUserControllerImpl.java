package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.common.dto.UserDataDto;
import com.aspire.asat.common.dto.notification.NotificationRecipientBundleDto;
import com.aspire.asat.common.dto.packages.UserSubPackageResponseDTO;
import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.registration.controller.EndUserController;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportOnboardResponseDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportRowStatus;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportUpdateRequestDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportUserDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportValidateResponseDto;
import com.aspire.asat.registration.data.endUser.request.EndUserRequestDTO;
import com.aspire.asat.registration.data.endUser.request.MigrateTrialUserRequestDto;
import com.aspire.asat.registration.data.endUser.request.UpdateRiskProfileExistRequestDto;
import com.aspire.asat.registration.data.endUser.request.UpdateUserRiskGroupRequestDto;
import com.aspire.asat.registration.data.endUser.request.UserSuspendRequestDto;
import com.aspire.asat.registration.data.endUser.request.UsersByIdsRequestDto;
import com.aspire.asat.registration.data.endUser.response.AspireUserBasicDto;
import com.aspire.asat.registration.data.endUser.response.EndUserResponseDTO;
import com.aspire.asat.registration.data.endUser.response.MigrateTrialUserResponseDto;
import com.aspire.asat.common.enums.ActivityType;
import com.aspire.asat.registration.data.endUser.response.PurchaseStatusResponseDTO;
import com.aspire.asat.registration.data.enums.RiskGroup;
import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.registration.data.request.EndUserUpdateRequestDTO;
import com.aspire.asat.registration.data.subpackage.SubPackageAssignRequest;
import com.aspire.asat.registration.exception.RegistationValidationException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.service.BulkUserImportService;
import com.aspire.asat.registration.service.EndUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EndUserControllerImpl implements EndUserController {

    private final EndUserService endUserService;
    private final BulkUserImportService bulkUserImportService;
    private final MessageService messageService;

    @Override
    @LogActivity(
            activityType = ActivityType.USER_CREATED,
            description = "Created end user: #{#requestDTO.email}"
    )
    public ResponseEntity<ApiResponseDto<EndUserResponseDTO>> addEndUser(EndUserRequestDTO requestDTO) {
        EndUserResponseDTO response = endUserService.addEndUser(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>(messageService.get(MessageKeys.USER_CREATED_SUCCESS), 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<EndUserResponseDTO>> getEndUserById(String userId) {
        Optional<EndUserResponseDTO> userOpt = endUserService.getEndUserById(userId);

        return userOpt.map(endUserResponseDTO -> ResponseEntity.ok(new ApiResponseDto<>("End user retrieved successfully", 200, endUserResponseDTO))).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponseDto<>("End user not found", 404, null)));

    }

    @Override
    @LogActivity(
            activityType = ActivityType.USER_UPDATED,
            description = "Updated end user: #{#requestDTO.id}",
            newValueExpression = "#{#requestDTO.firstName != null ? #requestDTO.firstName : (#requestDTO.lastName != null ? #requestDTO.lastName : #requestDTO.id)}"
    )
    public ResponseEntity<ApiResponseDto<EndUserResponseDTO>> updateEndUser(EndUserUpdateRequestDTO requestDTO) {
        try {
            EndUserResponseDTO response = endUserService.updateEndUser(requestDTO);
            return ResponseEntity.ok(new ApiResponseDto<>("End user updated successfully", 200, response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (IllegalStateException e) {
            log.error("End user update sync failed for id={}: {}", requestDTO.getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ApiResponseDto<>(e.getMessage(), 503, null));
        } catch (Exception e) {
            log.error("Error updating end user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to update end user", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<EndUserResponseDTO>>>> listEndUsers(
            String clientAdminId,
            String search,
            UserStatus status,
            List<String> departments,
            RiskGroup riskGroup,
            String productPackageId,
            int offset,
            int pageSize) {
        return buildEndUsersPagedResponse(
                clientAdminId,
                search,
                status,
                departments,
                riskGroup != null ? List.of(riskGroup) : null,
                offset,
                pageSize,
                productPackageId,
                "End users fetched");
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<EndUserResponseDTO>>>> listUsersByClientAdmin(
            String clientAdminId,
            List<String> departments,
            List<RiskGroup> riskGroups,
            int offset,
            int pageSize) {
        if (!StringUtils.hasText(clientAdminId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>("clientAdminId is required", 400, null));
        }
        return buildEndUsersPagedResponse(
                clientAdminId.trim(), null, UserStatus.ACTIVE, departments, riskGroups, offset, pageSize,
                null, "End users (USER) retrieved successfully");
    }

    /**
     * Shared by list and users-by-client-admin endpoints; delegates to EndUserService list/count with the same filter parameters.
     */
    private ResponseEntity<ApiResponseDto<AllResponseDto<List<EndUserResponseDTO>>>> buildEndUsersPagedResponse(
            String clientAdminId,
            String search,
            UserStatus status,
            List<String> departments,
            List<RiskGroup> riskGroups,
            int offset,
            int pageSize,
            String productPackageId,
            String successMessage) {
        try {
            AllResponseDto<List<EndUserResponseDTO>> body = endUserService.listAndCountEndUsers(
                    clientAdminId, search, status, departments, riskGroups, offset, pageSize, productPackageId);
            return ResponseEntity.ok(new ApiResponseDto<>(successMessage, 200, body));
        } catch (RegistationValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (IllegalStateException e) {
            log.error("End user list failed for clientAdminId={}: {}", clientAdminId, e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ApiResponseDto<>(e.getMessage(), 503, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<EndUserResponseDTO>>> importEndUsersFromCsv(MultipartFile file, String clientAdminId) {
        List<EndUserResponseDTO> skippedUsers = endUserService.importEndUsersFromCsv(file, clientAdminId);
        String responseMessage = messageService.get(MessageKeys.IMPORT_USERS_PARTIAL_SUCCESS);
        if (CollectionUtils.isEmpty(skippedUsers)) {
            responseMessage = "User's imported successfully";
        }
        return ResponseEntity.ok(new ApiResponseDto<>(responseMessage, HttpStatus.OK.value(), skippedUsers));
    }

    @Override
    public ResponseEntity<ApiResponseDto<BulkImportValidateResponseDto>> validateBulkImport(
            MultipartFile file, String clientAdminId, int offset, int pageSize) {
        BulkImportValidateResponseDto response =
                bulkUserImportService.validateImport(file, clientAdminId, offset, pageSize);
        return ResponseEntity.ok(new ApiResponseDto<>("File uploaded successfully and validated.", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<BulkImportUserDto>>>> getBulkImportUsers(
            String sessionId, BulkImportRowStatus status, int offset, int pageSize) {
        AllResponseDto<List<BulkImportUserDto>> response =
                bulkUserImportService.getSessionUsers(sessionId, status, offset, pageSize);
        return ResponseEntity.ok(new ApiResponseDto<>("Bulk import users retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<BulkImportValidateResponseDto>> updateBulkImportUsers(
            String sessionId, BulkImportUpdateRequestDto request, int offset, int pageSize) {
        BulkImportValidateResponseDto response =
                bulkUserImportService.updateSessionUsers(sessionId, request, offset, pageSize);
        return ResponseEntity.ok(new ApiResponseDto<>("Bulk import users updated successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<BulkImportOnboardResponseDto>> onboardBulkImport(String sessionId) {
        BulkImportOnboardResponseDto response = bulkUserImportService.onboardSession(sessionId);
        return ResponseEntity.ok(new ApiResponseDto<>("Bulk import completed", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> assignSubPackageToUsers(SubPackageAssignRequest subPackageAssignRequest) {
        try {
            endUserService.assignSubPackageToUsers(subPackageAssignRequest);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.PRODUCT_ASSIGNED_SUCCESS), 200, null));
        } catch (RegistationValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error assigning subpackage to users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to assign subpackage to users: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<EndUserResponseDTO>>>> getUnassignedUsersForProduct(
            String clientAdminId,
            String subPackageId,
            String search,
            UserStatus status,
            List<String> department,
            RiskGroup group,
            int offset,
            int pageSize) {

        List<EndUserResponseDTO> users = endUserService.getUnassignedUsersForProduct(
                clientAdminId, subPackageId, search, status, department, group, offset, pageSize
        );

        long total = endUserService.countUnassignedUsersForProduct(
                clientAdminId, subPackageId, search, status, department, group
        );

        AllResponseDto<List<EndUserResponseDTO>> body = new AllResponseDto<>(offset, pageSize, total, users);
        return ResponseEntity.ok(new ApiResponseDto<>("Unassigned users retrieved successfully", 200, body));
    }


    @Override
    public ResponseEntity<ApiResponseDto<Boolean>> userExistsByEmail(String email) {
        boolean exists = endUserService.userExistsByEmail(email.trim());
        return ResponseEntity.ok(new ApiResponseDto<>("User existence check completed", 200, exists));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<UserSubPackageResponseDTO>>> getUserAssignedSubPackages(String userId, String status) {
        log.info("Received request to get sub-packages for user: {} with status filter: {}", userId, status);
        List<UserSubPackageResponseDTO> subPackages = endUserService.getUserAssignedSubPackages(userId, status);

        String message = subPackages.isEmpty()
                ? "No sub-packages found for user" + (status != null ? " with status: " + status : "")
                : "Successfully retrieved " + subPackages.size() + " sub-packages for user" + (status != null ? " with status: " + status : "");

        ApiResponseDto<List<UserSubPackageResponseDTO>> response = new ApiResponseDto<>(
                message,
                HttpStatus.OK.value(),
                subPackages);

        log.info("Successfully processed request for user: {} - Found {} sub-packages", userId, subPackages.size());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<UserDataDto>> getUserDataById(String userId) {
        log.info("Received request to get user data for userId: {}", userId);
        Optional<UserDataDto> userDataOpt = endUserService.getUserDataById(userId);

        return userDataOpt.map(userData -> ResponseEntity.ok(new ApiResponseDto<>("User data retrieved successfully", 200, userData)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>("User not found", 404, null)));
    }

    @Override
    public ResponseEntity<ApiResponseDto<NotificationRecipientBundleDto>> getNotificationRecipientData(String userId) {
        log.info("Received request to get notification recipient data for userId: {}", userId);
        Optional<NotificationRecipientBundleDto> bundleOpt = endUserService.getNotificationRecipientBundle(userId);

        return bundleOpt.map(bundle -> ResponseEntity.ok(new ApiResponseDto<>("Notification recipient data retrieved successfully", 200, bundle)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponseDto<>("User not found", 404, null)));
    }

    @Override
    public ResponseEntity<ApiResponseDto<MigrateTrialUserResponseDto>> migrateTrialUser(MigrateTrialUserRequestDto requestDto) {
        try {
            log.info("Processing trial user migration for new clientAdminId: {}, email: {}", 
                    requestDto.getClientAdminId(), requestDto.getEmail());
            MigrateTrialUserResponseDto response = endUserService.migrateTrialUser(requestDto);
            return ResponseEntity.ok(new ApiResponseDto<>("Trial user migrated successfully", 200, response));
        } catch (ResourceNotFoundException e) {
            log.error("Trial client admin not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (IllegalArgumentException e) {
            log.error("Invalid migration request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error migrating trial user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to migrate trial user: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<EndUserResponseDTO>> updateUserStatus(
            String userId, 
            UserSuspendRequestDto requestDto) {
        try {
            log.info("Updating user status for userId: {} to status: {}", userId, requestDto.getStatus());
            EndUserResponseDTO response = endUserService.updateUserStatus(userId, requestDto);
            String message = "SUSPEND".equalsIgnoreCase(requestDto.getStatus()) 
                    ? "User suspended successfully" 
                    : "User activated successfully";
            return ResponseEntity.ok(new ApiResponseDto<>(message, 200, response));
        } catch (ResourceNotFoundException e) {
            log.error("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (IllegalArgumentException e) {
            log.error("Invalid status update request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error updating user status: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to update user status: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<EndUserResponseDTO>>>> getAllUsers(
            String clientAdminId, 
            String status, 
            int offset, 
            int pageSize) {
        try {
            log.info("Getting all users with filters - clientAdminId: {}, status: {}, offset: {}, pageSize: {}", 
                    clientAdminId, status, offset, pageSize);
            
            List<EndUserResponseDTO> users = endUserService.getAllUsers(clientAdminId, status, offset, pageSize);
            long total = endUserService.countAllUsers(clientAdminId, status);
            
            AllResponseDto<List<EndUserResponseDTO>> response = new AllResponseDto<>(offset, pageSize, total, users);
            return ResponseEntity.ok(new ApiResponseDto<>("Users retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting all users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve users: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<EndUserResponseDTO>>>> getAllAspireUsersWithFilters(
            String search,
            String userType,
            String country,
            String mspId,
            String clientAdminId,
            String status,
            int offset,
            int pageSize) {
        try {
            log.info("Getting all AspireUsers with filters - search: {}, userType: {}, country: {}, mspId: {}, clientAdminId: {}, status: {}, offset: {}, pageSize: {}", 
                    search, userType, country, mspId, clientAdminId, status, offset, pageSize);
            
            List<EndUserResponseDTO> users = endUserService.getAllAspireUsersWithFilters(
                    search, userType, country, mspId, clientAdminId, status, offset, pageSize);
            long total = endUserService.countAllAspireUsersWithFilters(
                    search, userType, country, mspId, clientAdminId, status);
            
            AllResponseDto<List<EndUserResponseDTO>> response = new AllResponseDto<>(offset, pageSize, total, users);
            return ResponseEntity.ok(new ApiResponseDto<>("AspireUsers retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting all AspireUsers with filters: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve AspireUsers: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<PurchaseStatusResponseDTO>> getPurchaseStatus(String email) {
        log.info("Received purchase status request for email: {}", email);
        PurchaseStatusResponseDTO response = endUserService.getPurchaseStatus(email.trim());
        return ResponseEntity.ok(new ApiResponseDto<>("Purchase status retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> updateRiskProfileExist(UpdateRiskProfileExistRequestDto request) {
        try {
            endUserService.updateRiskProfileExist(request);
            return ResponseEntity.ok(new ApiResponseDto<>("Risk profile exist flag updated successfully", 200, null));
        } catch (Exception e) {
            log.error("Error updating risk profile exist: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to update risk profile exist: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<EndUserResponseDTO>> updateUserRiskGroup(
            String userId,
            UpdateUserRiskGroupRequestDto request) {
        try {
            EndUserResponseDTO response = endUserService.updateUserRiskGroup(userId, request);
            return ResponseEntity.ok(new ApiResponseDto<>("User risk group updated successfully", 200, response));
        } catch (ResourceNotFoundException e) {
            log.error("User not found while updating risk group: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request while updating risk group: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error updating user risk group: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to update user risk group: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<AspireUserBasicDto>>> getUsersByIds(UsersByIdsRequestDto request) {
        try {
            List<AspireUserBasicDto> users = endUserService.getUsersByIds(request.getUserIds());
            return ResponseEntity.ok(new ApiResponseDto<>("Users retrieved successfully", 200, users));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error fetching users by IDs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve users: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<String>>> findEndUserIds(
            String clientAdminId,
            List<String> clientAdminIds,
            String search,
            List<String> departments) {
        try {
            List<String> scopedIds = new java.util.ArrayList<>();
            if (StringUtils.hasText(clientAdminId)) {
                scopedIds.add(clientAdminId.trim());
            }
            if (!CollectionUtils.isEmpty(clientAdminIds)) {
                scopedIds.addAll(clientAdminIds);
            }
            List<String> ids = endUserService.findEndUserIds(scopedIds, search, departments);
            return ResponseEntity.ok(new ApiResponseDto<>("User IDs retrieved successfully", 200, ids));
        } catch (RegistationValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error fetching end user IDs: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve user IDs: " + e.getMessage(), 500, null));
        }
    }
}
