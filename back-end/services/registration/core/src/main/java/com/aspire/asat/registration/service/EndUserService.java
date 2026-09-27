package com.aspire.asat.registration.service;

import com.aspire.asat.common.dto.UserDataDto;
import com.aspire.asat.common.dto.notification.NotificationRecipientBundleDto;
import com.aspire.asat.common.dto.packages.UserSubPackageResponseDTO;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.endUser.request.EndUserRequestDTO;
import com.aspire.asat.registration.data.endUser.request.MigrateTrialUserRequestDto;
import com.aspire.asat.registration.data.endUser.request.UpdateRiskProfileExistRequestDto;
import com.aspire.asat.registration.data.endUser.request.UpdateUserRiskGroupRequestDto;
import com.aspire.asat.registration.data.endUser.request.UserSuspendRequestDto;
import com.aspire.asat.registration.data.endUser.response.AspireUserBasicDto;
import com.aspire.asat.registration.data.endUser.response.EndUserResponseDTO;
import com.aspire.asat.registration.data.endUser.response.MigrateTrialUserResponseDto;
import com.aspire.asat.registration.data.endUser.response.PurchaseStatusResponseDTO;
import com.aspire.asat.registration.data.enums.RiskGroup;
import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.registration.data.request.EndUserUpdateRequestDTO;
import com.aspire.asat.registration.data.subpackage.SubPackageAssignRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface EndUserService {
    EndUserResponseDTO addEndUser(EndUserRequestDTO requestDTO);
    Optional<EndUserResponseDTO> getEndUserById(String userId);
    EndUserResponseDTO updateEndUser(EndUserUpdateRequestDTO requestDTO);
    List<EndUserResponseDTO> listEndUsers(String clientAdminId, String search, UserStatus status, List<String> departments, List<RiskGroup> riskGroups, int offset, int pageSize);
    long countEndUsers(String clientAdminId, String search, UserStatus status, List<String> departments, List<RiskGroup> riskGroups);

    /**
     * Paginated end users with optional exclusion of users already licensed for {@code productPackageId}.
     * One licensed-id fetch and one count/find snapshot per call when {@code productPackageId} is set.
     */
    AllResponseDto<List<EndUserResponseDTO>> listAndCountEndUsers(
            String clientAdminId,
            String search,
            UserStatus status,
            List<String> departments,
            List<RiskGroup> riskGroups,
            int offset,
            int pageSize,
            String productPackageId);

    List<EndUserResponseDTO> importEndUsersFromCsv(MultipartFile file, String clientAdminId);

    void assignSubPackageToUsers(SubPackageAssignRequest subPackageAssignRequest);

    List<EndUserResponseDTO> getUnassignedUsersForProduct(String clientAdminId, String subPackageId, String search, UserStatus status, List<String> departments, RiskGroup riskGroup, int offset, int pageSize);

    long countUnassignedUsersForProduct(String clientAdminId, String subPackageId, String search, UserStatus status, List<String> departments, RiskGroup riskGroup);


    boolean userExistsByEmail(String email);

    /**
     * Get sub-packages assigned to a specific user with optional status filtering
     * @param userId the user ID
     * @param status optional status filter (NOT_STARTED, IN_PROGRESS, COMPLETED). If null, returns all active packages.
     * @return list of sub-packages assigned to the user
     */
    List<UserSubPackageResponseDTO> getUserAssignedSubPackages(String userId, String status);

    /**
     * Get user data by userId including client admin information
     * @param userId the user ID
     * @return UserDataDto containing user information and client admin details, or empty if user not found
     */
    Optional<UserDataDto> getUserDataById(String userId);

    /**
     * Build the full notification recipient hierarchy for a user: the user themselves,
     * their Client Admin, their MSP, and the platform's active Aspire Admins.
     * Used by the notification service to fan a single event out to all applicable roles.
     * @param userId the end user's ID
     * @return the recipient bundle, or empty if the user cannot be resolved
     */
    Optional<NotificationRecipientBundleDto> getNotificationRecipientBundle(String userId);

    /**
     * Migrate trial user account to regular account
     * @param requestDto migration request containing new clientAdminId, email, and optional domain
     * @return Migration response with details of migrated users and deactivated subpackages
     */
    MigrateTrialUserResponseDto migrateTrialUser(MigrateTrialUserRequestDto requestDto);

    /**
     * Update user status (SUSPEND or ACTIVE) and send notification
     * @param userId the user ID
     * @param requestDto request containing status and optional suspendReason
     * @return Updated end user response DTO
     */
    EndUserResponseDTO updateUserStatus(String userId, UserSuspendRequestDto requestDto);

    /**
     * Get all users with pagination and filters
     * @param clientAdminId Optional filter by client admin ID
     * @param status Optional filter by status
     * @param offset Page offset
     * @param pageSize Page size
     * @return List of end user response DTOs
     */
    List<EndUserResponseDTO> getAllUsers(String clientAdminId, String status, int offset, int pageSize);

    /**
     * Count all users with filters
     * @param clientAdminId Optional filter by client admin ID
     * @param status Optional filter by status
     * @return Total count of users matching the filters
     */
    long countAllUsers(String clientAdminId, String status);

    /**
     * Get all AspireUsers with advanced filters
     * @param search Optional search by email (username field) - case insensitive partial match
     * @param userType Optional filter by userType (MSP, CLIENT_ADMIN, USER, etc.)
     * @param country Optional filter by country
     * @param mspId Optional filter by MSP ID
     * @param clientAdminId Optional filter by client admin ID
     * @param status Optional filter by status
     * @param offset Page offset
     * @param pageSize Page size
     * @return List of end user response DTOs
     */
    List<EndUserResponseDTO> getAllAspireUsersWithFilters(
            String search,
            String userType,
            String country,
            String mspId,
            String clientAdminId,
            String status,
            int offset,
            int pageSize);

    /**
     * Count all AspireUsers with advanced filters
     * @param search Optional search by email (username field) - case insensitive partial match
     * @param userType Optional filter by userType (MSP, CLIENT_ADMIN, USER, etc.)
     * @param country Optional filter by country
     * @param mspId Optional filter by MSP ID
     * @param clientAdminId Optional filter by client admin ID
     * @param status Optional filter by status
     * @return Total count of users matching the filters
     */
    long countAllAspireUsersWithFilters(
            String search,
            String userType,
            String country,
            String mspId,
            String clientAdminId,
            String status);

    /**
     * Get purchase status for a user by email
     * @param email the email address to check
     * @return PurchaseStatusResponseDTO containing trailExists, buyNowExists, and alreadyRegistered flags
     */
   PurchaseStatusResponseDTO getPurchaseStatus(String email);

    /**
     * Bulk update isRiskProfileExist flag for users by list of user IDs.
     * @param request request containing list of userIds and isRiskProfileExist value
     */
    void updateRiskProfileExist(UpdateRiskProfileExistRequestDto request);

    /**
     * Update risk group for a single user by userId.
     * @param userId user identifier (UUID string)
     * @param request request containing risk group value
     * @return updated user as EndUserResponseDTO
     */
    EndUserResponseDTO updateUserRiskGroup(String userId, UpdateUserRiskGroupRequestDto request);

    /**
     * Fetch basic AspireUser details (userId, email, fullName) for a list of user IDs.
     */
    List<AspireUserBasicDto> getUsersByIds(List<String> userIds);

    /**
     * Unpaginated USER-type IDs matching search (name/email) and/or department for the given client admins.
     */
    List<String> findEndUserIds(List<String> clientAdminIds, String search, List<String> departments);
}


