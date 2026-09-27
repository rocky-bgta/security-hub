package com.aspire.asat.registration.controller;

import com.aspire.asat.common.dto.UserDataDto;
import com.aspire.asat.common.dto.notification.NotificationRecipientBundleDto;
import com.aspire.asat.common.dto.packages.UserSubPackageResponseDTO;
import com.aspire.asat.registration.constant.WebApiUrlConstants;
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
import com.aspire.asat.registration.data.endUser.response.PurchaseStatusResponseDTO;
import com.aspire.asat.registration.data.enums.RiskGroup;
import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.registration.data.request.EndUserUpdateRequestDTO;
import com.aspire.asat.registration.data.subpackage.SubPackageAssignRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping(WebApiUrlConstants.END_USER)
@Tag(name = "End User Management", description = "Manage end users under client admins (create, list, import via CSV or Excel)")
public interface EndUserController {

    @Operation(
            summary = "Add a new end user",
            description = "Registers a new end user under a specific client admin. A temporary password will be generated and stored securely."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "End user added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<EndUserResponseDTO>> addEndUser(
            @Parameter(description = "Request body containing end user details", required = true)
            @Valid @RequestBody EndUserRequestDTO requestDTO
    );

    @Operation(
            summary = "Get end user by ID",
            description = "Retrieves a specific end user by their unique identifier."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "End user retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "End user not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid user ID", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{userId}")
    ResponseEntity<ApiResponseDto<EndUserResponseDTO>> getEndUserById(
            @Parameter(description = "Unique identifier of the end user", required = true)
            @PathVariable String userId
    );

    @Operation(
            summary = "Update end user",
            description = "Updates an existing end user's information. The client admin must have permission to update this user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "End user updated successfully"),
            @ApiResponse(responseCode = "404", description = "End user not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input or permission denied", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping
    ResponseEntity<ApiResponseDto<EndUserResponseDTO>> updateEndUser(
            @Parameter(description = "Request body containing updated end user details", required = true)
            @Valid @RequestBody EndUserUpdateRequestDTO requestDTO
    );

    @Operation(
            summary = "List end users",
            description = "Retrieves a paginated list of end users under a given client admin. Supports filtering by status, department, optional risk group, and searching by name or email."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "End users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<EndUserResponseDTO>>>> listEndUsers(
            @Parameter(description = "Client admin ID to filter users", required = true)
            @RequestParam String clientAdminId,
            @Parameter(description = "Search term to filter by name or email")
            @RequestParam(required = false) String search,
            @Parameter(description = "User status to filter by")
            @RequestParam(required = false) UserStatus status,
            @Parameter(description = "List of department to filter by (supports multiple department)")
            @RequestParam(required = false) List<String> departments,
            @Parameter(description = "Risk group to filter by")
            @RequestParam(required = false) RiskGroup riskGroup,
            @Parameter(description = "ClientProduct.id; excludes users already licensed for this package")
            @RequestParam(required = false) String productPackageId,
            @Parameter(description = "Page offset for pagination", example = "0")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Page size for pagination", example = "10")
            @RequestParam(defaultValue = "10") int pageSize
    );

    @Operation(
            summary = "List end users (USER type only)",
            description = "Returns a paginated list of Aspire users with userType USER for the given client admin. "
                    + "Optional filters: department (repeat param for multiple), risk groups (repeat param for multiple)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g. missing clientAdminId)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/users")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<EndUserResponseDTO>>>> listUsersByClientAdmin(
            @Parameter(description = "Client admin ID (required)", required = true)
            @RequestParam String clientAdminId,
            @Parameter(description = "Filter by department name (repeat for multiple)")
            @RequestParam(required = false) List<String> departments,
            @Parameter(description = "Risk group(s); repeat query param for multiple (e.g. riskGroup=HIGH_RISK&riskGroup=LOW_RISK)")
            @RequestParam(name = "riskGroup", required = false) List<RiskGroup> riskGroups,
            @Parameter(description = "Page offset", example = "0")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int pageSize
    );

    @Operation(
            summary = "Import end users from CSV or Excel",
            description = "Accepts .csv, .xls, or .xlsx files. Skips users with duplicate emails and returns those."
    )
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponseDto<List<EndUserResponseDTO>>> importEndUsersFromCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("clientAdminId") String clientAdminId);

    @Operation(
            summary = "Step 1: Validate bulk end-user import file",
            description = "Step 1 of bulk user onboarding. Upload and validate a CSV/Excel file without creating users. "
                    + "Returns an importSessionId plus paginated valid and invalid user lists for review."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File validated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping(value = "/bulk-import/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponseDto<BulkImportValidateResponseDto>> validateBulkImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam("clientAdminId") String clientAdminId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int pageSize);

    @Operation(
            summary = "Step 2: List validated bulk-import users",
            description = "Step 2 of bulk user onboarding. Returns a paginated list of valid or invalid users "
                    + "from an import session (status=VALID or INVALID) for admin review."
    )
    @GetMapping("/bulk-import/{sessionId}/users")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<BulkImportUserDto>>>> getBulkImportUsers(
            @PathVariable String sessionId,
            @RequestParam BulkImportRowStatus status,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int pageSize);

    @Operation(
            summary = "Step 3: Update bulk-import session users",
            description = "Step 3 of bulk user onboarding. Apply admin corrections to invalid (or other) session rows, "
                    + "revalidate the session, and return updated paginated valid/invalid lists."
    )
    @PutMapping("/bulk-import/{sessionId}/users")
    ResponseEntity<ApiResponseDto<BulkImportValidateResponseDto>> updateBulkImportUsers(
            @PathVariable String sessionId,
            @Valid @RequestBody BulkImportUpdateRequestDto request,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int pageSize);

    @Operation(
            summary = "Step 4: Onboard users from a bulk-import session",
            description = "Step 4 of bulk user onboarding. Creates all currently valid users in the session, "
                    + "skips failures, sends the bulk import summary notification, and returns the onboard summary."
    )
    @PostMapping("/bulk-import/{sessionId}/onboard")
    ResponseEntity<ApiResponseDto<BulkImportOnboardResponseDto>> onboardBulkImport(
            @PathVariable String sessionId);

    @Operation(
            summary = "Assign product to multiple end users",
            description = "Creates a relationship between each provided end user and the specified product."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product assigned to users successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or missing parameters", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/assign-sub-package")
    ResponseEntity<ApiResponseDto<Void>> assignSubPackageToUsers(@Valid @RequestBody SubPackageAssignRequest subPackageAssignRequest);

    @Operation(
            summary = "List end users not assigned to a product",
            description = "Returns a paginated list of end users under the given client admin who are NOT assigned to the specified product. Supports search, status, department, and risk group filters."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unassigned users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/unassigned")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<EndUserResponseDTO>>>> getUnassignedUsersForProduct(
            @RequestParam String clientAdminId,
            @RequestParam String subPackageId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserStatus status,
            @Parameter(description = "List of department to filter by (supports multiple department)")
            @RequestParam(required = false) List<String> department,
            @RequestParam(required = false) RiskGroup group,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int pageSize
    );


    @Operation(
            summary = "Check if end user exists by email for all types of Users",
            description = "Returns true if an end user with the given email exists under the specified client admin."
    )
    @GetMapping("/exists-by-email")
    ResponseEntity<ApiResponseDto<Boolean>> userExistsByEmail(@RequestParam String email);

    @Operation(
            summary = "Get user assigned sub-packages Both Internal and External uses ",
            description = "Retrieves sub-packages assigned to a specific user with optional status filtering."
    )
    @GetMapping("/{userId}/sub-packages")
    ResponseEntity<ApiResponseDto<List<UserSubPackageResponseDTO>>> getUserAssignedSubPackages(
            @Parameter(description = "Unique identifier of the user", required = true)
            @PathVariable String userId,
            @Parameter(description = "Filter by package status (NOT_STARTED, IN_PROGRESS, COMPLETED). If not provided, returns all active packages.")
            @RequestParam(required = false) String status
    );

    @Operation(
            summary = "Get user data by ID",
            description = "Retrieves user data including client admin information by user ID. Used for notification purposes and other user-related operations."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User data retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid user ID", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{userId}/user-data")
    ResponseEntity<ApiResponseDto<UserDataDto>> getUserDataById(
            @Parameter(description = "Unique identifier of the user", required = true)
            @PathVariable String userId
    );

    @Operation(
            summary = "Get notification recipient hierarchy for a user",
            description = "Retrieves the full recipient hierarchy (user, client admin, MSP, and active Aspire Admins) " +
                    "for role-based notification fan-out. Used by the notification service."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification recipient data retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid user ID", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/notification-data")
    ResponseEntity<ApiResponseDto<NotificationRecipientBundleDto>> getNotificationRecipientData(
            @Parameter(description = "Unique identifier of the user", required = true)
            @RequestParam String userId
    );

    @Operation(
            summary = "Migrate trial user to regular user",
            description = "Migrates a trial user account to a regular account when a new admin from the same company purchases a plan. Deactivates trial subpackages and migrates all users to the new client admin."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trial user migrated successfully"),
            @ApiResponse(responseCode = "404", description = "Trial client admin not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input or migration failed", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/migrate-trial-user")
    ResponseEntity<ApiResponseDto<MigrateTrialUserResponseDto>> migrateTrialUser(
            @Parameter(description = "Request body containing migration details", required = true)
            @Valid @RequestBody MigrateTrialUserRequestDto requestDto
    );

    @Operation(
            summary = "Update user status (Suspend or Activate)",
            description = "Updates a user's status to SUSPEND or ACTIVE. When suspending, a suspend reason is required. When activating, suspend reason is optional. Sends an email notification to the user about the status change."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User status updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input (status must be SUSPEND or ACTIVE, suspend reason required for SUSPEND)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{userId}/status")
    ResponseEntity<ApiResponseDto<EndUserResponseDTO>> updateUserStatus(
            @Parameter(description = "Unique identifier of the user", required = true)
            @PathVariable String userId,
            @Parameter(description = "Request body containing status and optional suspend reason", required = true)
            @Valid @RequestBody UserSuspendRequestDto requestDto
    );

    @Operation(
            summary = "Get all users with pagination",
            description = "Retrieves a paginated list of all users from AspireUser repository. Supports filtering by clientAdminId and status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/all")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<EndUserResponseDTO>>>> getAllUsers(
            @Parameter(description = "Filter by client admin ID", example = "client-admin-123")
            @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "Filter by user status", example = "ACTIVE")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page offset (default: 0)", example = "0")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Page size (default: 10)", example = "10")
            @RequestParam(defaultValue = "10") int pageSize
    );

    @Operation(
            summary = "Get all AspireUsers with advanced filters",
            description = "Retrieves a paginated list of all users from AspireUser table with advanced filtering options. Supports search by email (username field), filtering by userType (MSP, CLIENT_ADMIN, USER, etc.), country, mspId, clientAdminId, and status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/aspire-users")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<EndUserResponseDTO>>>> getAllAspireUsersWithFilters(
            @Parameter(description = "Search by user email or username (case insensitive partial match)", example = "user@example.com")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by user type (MSP, CLIENT_ADMIN, USER, ASPIRE_ADMIN, etc.)", example = "USER")
            @RequestParam(required = false) String userType,
            @Parameter(description = "Filter by countryId", example = "countryId-123")
            @RequestParam(required = false) String country,
            @Parameter(description = "Filter by MSP ID", example = "msp-123")
            @RequestParam(required = false) String mspId,
            @Parameter(description = "Filter by client admin ID", example = "client-admin-123")
            @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "Filter by user status (ACTIVE, INACTIVE, SUSPEND, etc.)", example = "ACTIVE")
            @RequestParam(required = false) String status,
            @Parameter(description = "Page offset (default: 0)", example = "0")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Page size (default: 10)", example = "10")
            @RequestParam(defaultValue = "10") int pageSize
    );

    @Operation(
            summary = "Get purchase status by email -  Public API",
            description = "Returns purchase status information including trailExists, buyNowExists, and alreadyRegistered flags for a given email address. This is a public API."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Purchase status retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid email parameter", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/purchase-status")
    ResponseEntity<ApiResponseDto<PurchaseStatusResponseDTO>> getPurchaseStatus(
            @Parameter(description = "Email address to check purchase status", required = true)
            @RequestParam String email
    );

    @Operation(
            summary = "Bulk update risk profile exist flag",
            description = "Updates isRiskProfileExist for all users in the given list of user IDs. Fetches users by userId, sets the flag and saves."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Risk profile exist flag updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g. empty userIds)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/risk-profile-exist")
    ResponseEntity<ApiResponseDto<Void>> updateRiskProfileExist(
            @Parameter(description = "Request body with list of userIds and isRiskProfileExist flag", required = true)
            @Valid @RequestBody UpdateRiskProfileExistRequestDto request
    );

    @Operation(
            summary = "Update user risk group",
            description = "Updates risk group for a single user in AspireUser."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User risk group updated successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PutMapping("/{userId}/risk-group")
    ResponseEntity<ApiResponseDto<EndUserResponseDTO>> updateUserRiskGroup(
            @Parameter(description = "Unique identifier of the user", required = true)
            @PathVariable String userId,
            @Parameter(description = "Request body containing new risk group", required = true)
            @Valid @RequestBody UpdateUserRiskGroupRequestDto request
    );

    @Operation(
            summary = "Get users by IDs",
            description = "Fetches AspireUser basic details (userId, email, fullName) for the given list of user IDs."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g. empty userIds)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/by-ids")
    ResponseEntity<ApiResponseDto<List<AspireUserBasicDto>>> getUsersByIds(
            @Valid @RequestBody UsersByIdsRequestDto request
    );

    @Operation(
            summary = "Get end-user IDs by search and department",
            description = "Returns unpaginated USER-type IDs for the given client admin(s). "
                    + "Search matches first name, last name, and email. Department is a case-insensitive exact match. "
                    + "Used by CMS License Assignments to pre-filter assignment rows."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User IDs retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request (e.g. missing clientAdminId)", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/ids")
    ResponseEntity<ApiResponseDto<List<String>>> findEndUserIds(
            @Parameter(description = "Client admin ID (repeat or combine with clientAdminIds)")
            @RequestParam(required = false) String clientAdminId,
            @Parameter(description = "Client admin IDs (MSP multi-client scope)")
            @RequestParam(required = false) List<String> clientAdminIds,
            @Parameter(description = "Search term to filter by name or email")
            @RequestParam(required = false) String search,
            @Parameter(description = "Department names (repeat for multiple)")
            @RequestParam(required = false) List<String> departments
    );
}
