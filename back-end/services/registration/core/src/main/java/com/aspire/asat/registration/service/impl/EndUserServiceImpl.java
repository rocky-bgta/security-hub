package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.dto.UserDataDto;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.dto.notification.NotificationRecipientBundleDto;
import com.aspire.asat.common.dto.notification.NotificationRecipientContactDto;
import com.aspire.asat.common.dto.packages.UserSubPackageResponseDTO;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.common.exception.ResourceAlreadyExistsException;
import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.registration.client.service.PhishingServiceClient;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.clientAdmin.request.AdminStatus;
import com.aspire.asat.registration.data.cms.request.CmsUserSubPackageAssignRequest;
import com.aspire.asat.registration.data.dto.AspireUserCreateRequestDto;
import com.aspire.asat.registration.data.dto.AspireUserDto;
import com.aspire.asat.registration.data.dto.AspireUserUpdateRequestDto;
import com.aspire.asat.registration.data.endUser.request.EndUserRequestDTO;
import com.aspire.asat.registration.data.endUser.request.MigrateTrialUserRequestDto;
import com.aspire.asat.registration.data.endUser.request.UpdateRiskProfileExistRequestDto;
import com.aspire.asat.registration.data.endUser.request.UpdateUserRiskGroupRequestDto;
import com.aspire.asat.registration.data.endUser.request.UserSuspendRequestDto;
import com.aspire.asat.registration.data.endUser.response.AspireUserBasicDto;
import com.aspire.asat.registration.data.endUser.response.EndUserResponseDTO;
import com.aspire.asat.registration.data.endUser.response.MigrateTrialUserResponseDto;
import com.aspire.asat.registration.data.endUser.response.PurchaseStatusResponseDTO;
import com.aspire.asat.registration.data.enums.LicenceStatus;
import com.aspire.asat.registration.data.enums.OnboardBy;
import com.aspire.asat.registration.data.enums.PackageStatus;
import com.aspire.asat.registration.data.enums.RiskGroup;
import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.registration.data.phishing.request.UserLicenceSnapshotRequestDto;
import com.aspire.asat.registration.data.request.EndUserUpdateRequestDTO;
import com.aspire.asat.registration.data.subpackage.SubPackageAssignRequest;
import com.aspire.asat.registration.dto.notification.BulkImportSummaryDto;
import com.aspire.asat.registration.dto.notification.NewUserNotificationDto;
import com.aspire.asat.registration.dto.notification.PackageAssignmentNotificationDto;
import com.aspire.asat.registration.exception.CustomException;
import com.aspire.asat.registration.exception.RegistationValidationException;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.mapper.AspireUserMapper;
import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.model.ClientProduct;
import com.aspire.asat.registration.model.EndUserPackage;
import com.aspire.asat.registration.model.Role;
import com.aspire.asat.registration.model.UserLicence;
import com.aspire.asat.registration.model.UserSuspendReason;
import com.aspire.asat.registration.model.dropdown.Country;
import com.aspire.asat.registration.model.msp.MspUser;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.ClientProductRepository;
import com.aspire.asat.registration.repository.EndUserPackageRepository;
import com.aspire.asat.registration.repository.RoleRepository;
import com.aspire.asat.registration.repository.msp.MspUsersRepository;
import com.aspire.asat.registration.repository.UserLicenceRepository;
import com.aspire.asat.registration.repository.UserSuspendReasonRepository;
import com.aspire.asat.registration.repository.custom.EndUserRepositoryCustom;
import com.aspire.asat.registration.repository.dropdown.CountryRepository;
import com.aspire.asat.registration.repository.dropdown.TimezoneRepository;
import com.aspire.asat.registration.service.AspireUserService;
import com.aspire.asat.registration.service.DepartmentService;
import com.aspire.asat.registration.service.EndUserService;
import com.aspire.asat.registration.service.support.BulkImportFileParser;
import com.aspire.asat.registration.service.support.SimulationProductResolver;
import com.aspire.asat.registration.service.support.UserSessionInvalidationHelper;
import com.aspire.asat.registration.utils.CommonUtils;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EndUserServiceImpl implements EndUserService {

    private final PasswordEncoder passwordEncoder;
    private final EndUserPackageRepository endUserPackageRepository;
    private final AspireUserService aspireUserService;
    private final DepartmentService departmentService;
    private final RoleRepository roleRepository;
    private final ClientProductRepository clientProductRepository;
    private final CountryRepository countryRepository;
    private final WebClient webClient;
    private final UserLicenceRepository userLicenceRepository;
    private final UserCurrentContextService currentContextService;
    private final RegistrationNotificationClient notificationClient;
    private final AspireUserRepository aspireUserRepository;
    private final ClientAdminRepository clientAdminRepository;
    private final MspUsersRepository mspUsersRepository;
    private final UserSuspendReasonRepository userSuspendReasonRepository;
    private final MongoTemplate mongoTemplate;
    private final EndUserRepositoryCustom endUserRepositoryCustom;
    private final UserSessionInvalidationHelper userSessionInvalidationHelper;
    private final TimezoneRepository timezoneRepository;
    private final PhishingServiceClient phishingServiceClient;
    private final AspireUserMapper aspireUserMapper;
    private final SimulationProductResolver simulationProductResolver;
    private final BulkImportFileParser bulkImportFileParser;

    @Value("${service.cms.url}")
    private String cmsServiceUrl;

    private String generateRandomPassword() {
        return CommonUtils.generateTemporaryPassword();
    }


    private List<String> convertRoleNamesToIds(List<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return List.of();
        }

        return roleNames.stream()
                .map(roleName -> {
                    Role role = roleRepository.findByRoleName(roleName);
                    return role != null ? role.getId() : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }


    @Override
    public EndUserResponseDTO addEndUser(EndUserRequestDTO requestDTO) {
        log.info("Creating end user with email: {}", requestDTO.getEmail());

        // Check if a user already exists by email
        Optional<AspireUserDto> existingUser = aspireUserService.getUserByEmail(requestDTO.getEmail().trim());
        if (existingUser.isPresent()) {
            throw new ResourceAlreadyExistsException(MessageKeys.USER_ALREADY_EXISTS);
        }

        // Validate email domain matches client admin's company email domain
        validateEmailDomain(requestDTO.getEmail(), requestDTO.getClientAdminId());

        // 3. Assign a default role ID for end users
        final var roles = convertRoleNamesToIds(List.of(UserType.USER.name()));

        // 4. Determine the final status
        UserStatus finalStatus = requestDTO.getStatus() != null
                ? requestDTO.getStatus()
                : UserStatus.ACTIVE;

        // 5. Get firstName and lastName from request
        String firstName = requestDTO.getFirstName().trim();
        String lastName = requestDTO.getLastName() != null ? requestDTO.getLastName().trim() : "";

        // 6. Find or create a department
        String departmentName = requestDTO.getDepartment();
        departmentService.findOrCreateDepartment(departmentName, requestDTO.getClientAdminId());

        // 7. Create AspireUser record
        UUID endUserId = UUID.randomUUID();
        AspireUserCreateRequestDto aspireUserRequest = AspireUserCreateRequestDto.builder()
                .baseUserId(endUserId)
                .firstName(firstName)
                .lastName(lastName)
                .email(requestDTO.getEmail())
                .password(null)
                .phoneNumber(requestDTO.getPhoneNumber())
                .phoneCode(requestDTO.getPhoneCode())
                .countryCode(requestDTO.getCountryCode())
                .country(getCountry(requestDTO.getCountryCode()))
                .roles(roles)
                .userType(UserType.USER.name())
                .status(finalStatus.name())
                .createdBy(requestDTO.getClientAdminId()) // Set the client admin as the creator
                .plainPassword(null) // Store plain password for email
                .clientAdminId(requestDTO.getClientAdminId())
                .department(requestDTO.getDepartment())
                .riskGroup(RiskGroup.HIGH_RISK) // Set the default risk group
                .isCredentialSent(false)
                .build();

        // 7. Create the user in the aspire_user table
        AspireUserDto aspireUser = aspireUserService.createUser(aspireUserRequest);
        log.info("Created AspireUser with ID: {} for end user: {}", aspireUser.getBaseUserId(), requestDTO.getEmail());

        // 8. Send email notification with a temporary password
        String fullName = firstName + (lastName.isEmpty() ? "" : " " + lastName);
        try {
            notificationClient.sendWelcomeEmaiWithoutPasswordlNotification(
                    requestDTO.getEmail(),
                    endUserId.toString(),
                    requestDTO.getClientAdminId(), // clientAdminId for client-specific notification settings
                    fullName,
                    null);
        } catch (Exception e) {
            log.warn("Failed to send welcome email notification for user {}: {}", requestDTO.getEmail(), e.getMessage());
        }
        aspireUserRepository.findByUserId(UUID.fromString(requestDTO.getClientAdminId()))
                .ifPresent(adminUser -> {
                    NewUserNotificationDto notificationDto = NewUserNotificationDto.builder()
                            .userEmail(requestDTO.getEmail())
                            .userId(adminUser.getUserId().toString())
                            .userName(fullName)
                            .registrationDate(aspireUser.getCreatedAt())
                            .adminName(adminUser.getFirstName() + " " + adminUser.getLastName())
                            .adminEmail(adminUser.getEmail())
                            .clientAdminId(requestDTO.getClientAdminId()) // clientAdminId for client-specific notification settings
                            .build();
                    try {
                        notificationClient.sendNewUserNotificationToAdmin(notificationDto);
                    } catch (Exception e) {
                        log.warn("Failed to send new user notification to admin for user {}: {}", requestDTO.getEmail(), e.getMessage());
                    }
                });

        // 9. Prepare and return the response DTO
        return EndUserResponseDTO.builder()
                .id(aspireUser.getBaseUserId().toString())
                .firstName(firstName)
                .lastName(lastName)
                .fullName(fullName)
                .email(requestDTO.getEmail())
                .phoneNumber(requestDTO.getPhoneNumber())
                .phoneCode(requestDTO.getPhoneCode())
                .status(aspireUser.getStatus() != null ? UserStatus.fromString(aspireUser.getStatus()) : null)
                .department(requestDTO.getDepartment())
                .clientAdminId(requestDTO.getClientAdminId())
                .riskGroup(aspireUser.getRiskGroup())
                .build();
    }

    private String getCountry(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            return "";
        }
        return countryRepository.findActiveByCode(countryCode.trim())
                .map(Country::getName)
                .orElse("");
    }

    @Override
    public Optional<EndUserResponseDTO> getEndUserById(String userId) {
        log.info("Getting end user by ID: {}", userId);

        try {
            Optional<AspireUserDto> aspireUserOpt = aspireUserService.getUserById(UUID.fromString(userId));

            if (aspireUserOpt.isEmpty()) {
                log.warn("End user not found with ID: {}", userId);
                return Optional.empty();
            }

            AspireUserDto aspireUser = aspireUserOpt.get();

            // Verify this is a USER type and belongs to the expected client admin
            if (!UserType.USER.name().equals(aspireUser.getUserType())) {
                log.warn("User with ID {} is not an end user (userType: {})", userId, aspireUser.getUserType());
                return Optional.empty();
            }

            EndUserResponseDTO responseDTO = convertToEndUserResponseDTO(aspireUser);
            log.info("Successfully retrieved end user: {}", responseDTO.getEmail());
            return Optional.of(responseDTO);

        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for userId: {}", userId, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting end user by ID: {}", userId, e);
            return Optional.empty();
        }
    }

    @Override
    public EndUserResponseDTO updateEndUser(EndUserUpdateRequestDTO requestDTO) {
        log.info("Updating end user with ID: {}", requestDTO.getId());

        try {
            // Get the existing user
            Optional<AspireUserDto> existingUserOpt = aspireUserService.getUserById(UUID.fromString(requestDTO.getId()));

            if (existingUserOpt.isEmpty()) {
                throw new IllegalArgumentException("End user not found with ID: " + requestDTO.getId());
            }

            AspireUserDto existingUser = existingUserOpt.get();

            // Create update request builder
            AspireUserUpdateRequestDto.AspireUserUpdateRequestDtoBuilder updateRequestBuilder = AspireUserUpdateRequestDto.builder();

            // Apply optional fields via small helpers to reduce cognitive complexity
            setNameIfPresent(updateRequestBuilder, requestDTO.getFirstName(), requestDTO.getLastName());
            applyIfPresent(requestDTO.getPhoneNumber(), updateRequestBuilder::phoneNumber);
            applyIfPresent(requestDTO.getDepartment(), updateRequestBuilder::department);
            applyCountryIfPresent(updateRequestBuilder, requestDTO.getCountryCode());
            applyIfPresent(requestDTO.getProfilePicture(), updateRequestBuilder::profilePicture);
            applyIfPresent(requestDTO.getStatus(), updateRequestBuilder::status);

            CurrentUserContext context = currentContextService.getCurrentUserContext();
            updateRequestBuilder.updateBy(context.getUserId());

            // Build the update request
            AspireUserUpdateRequestDto updateRequest = updateRequestBuilder.build();

            // Update the user
            AspireUserDto updatedUser = aspireUserService.updateUser(existingUser.getBaseUserId(), updateRequest);
            log.info("Successfully updated end user: {}", updatedUser.getEmail());

            if (hasText(requestDTO.getStatus())) {
                String userIdForLogout = updatedUser.getBaseUserId() != null
                        ? updatedUser.getBaseUserId().toString()
                        : requestDTO.getId();
                userSessionInvalidationHelper.logoutUsersIfRestrictive(
                        requestDTO.getStatus().trim(),
                        List.of(userIdForLogout));
            }

            syncLicensedUserSnapshotIfNeeded(requestDTO, updatedUser);

            // Convert to response DTO
            return convertToEndUserResponseDTO(updatedUser);

        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Invalid request for updating end user: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating end user with ID: {}", requestDTO.getId(), e);
            throw new CustomException("Failed to update end user", e);
        }
    }

    private void syncLicensedUserSnapshotIfNeeded(EndUserUpdateRequestDTO requestDTO, AspireUserDto updatedUser) {
        if (!hasLicenceRelevantUpdate(requestDTO)) {
            return;
        }
        if (updatedUser.getBaseUserId() == null || !hasText(updatedUser.getClientAdminId())) {
            log.warn("Skipping licence snapshot sync: missing userId or clientAdminId for request id={}",
                    requestDTO.getId());
            return;
        }

        boolean active = updatedUser.getStatus() != null
                && "ACTIVE".equalsIgnoreCase(updatedUser.getStatus().trim());

        phishingServiceClient.syncLicensedUserSnapshot(UserLicenceSnapshotRequestDto.builder()
                .userId(updatedUser.getBaseUserId().toString())
                .clientAdminId(updatedUser.getClientAdminId().trim())
                .firstName(updatedUser.getFirstName())
                .lastName(updatedUser.getLastName())
                .phoneNumber(updatedUser.getPhoneNumber())
                .departmentName(updatedUser.getDepartment())
                .countryName(updatedUser.getCountry())
                .active(active)
                .build());
    }

    private boolean hasLicenceRelevantUpdate(EndUserUpdateRequestDTO requestDTO) {
        return hasText(requestDTO.getFirstName())
                || hasText(requestDTO.getLastName())
                || hasText(requestDTO.getPhoneNumber())
                || hasText(requestDTO.getDepartment())
                || hasText(requestDTO.getCountryCode())
                || hasText(requestDTO.getStatus());
    }

    private void setNameIfPresent(AspireUserUpdateRequestDto.AspireUserUpdateRequestDtoBuilder builder, String firstName, String lastName) {
        if (hasText(firstName)) {
            builder.firstName(firstName.trim());
        }
        if (hasText(lastName)) {
            builder.lastName(lastName.trim());
        }
    }

    private void applyIfPresent(String value, java.util.function.Consumer<String> setter) {
        if (!hasText(value)) return;
        setter.accept(value.trim());
    }

    private void applyCountryIfPresent(AspireUserUpdateRequestDto.AspireUserUpdateRequestDtoBuilder builder, String countryCode) throws BadRequestException {
        if (!hasText(countryCode)) return;
        String resolvedCountryCode = countryCode.trim();
        if (!countryRepository.existsByCode(resolvedCountryCode)) {
            throw new BadRequestException("Invalid country code: " + countryCode);
        }
        String countryName = getCountry(resolvedCountryCode);
        builder.countryCode(resolvedCountryCode).country(countryName);
    }

    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }


    @Override
    public List<EndUserResponseDTO> listEndUsers(String clientAdminId, String search, UserStatus status, List<String> departments, List<RiskGroup> riskGroups, int offset, int pageSize) {
        return listAndCountEndUsers(clientAdminId, search, status, departments, riskGroups, offset, pageSize, null)
                .getItems();
    }

    @Override
    public long countEndUsers(String clientAdminId, String search, UserStatus status, List<String> departments, List<RiskGroup> riskGroups) {
        return listAndCountEndUsers(clientAdminId, search, status, departments, riskGroups, 0, 1, null)
                .getTotal();
    }

    @Override
    public AllResponseDto<List<EndUserResponseDTO>> listAndCountEndUsers(
            String clientAdminId,
            String search,
            UserStatus status,
            List<String> departments,
            List<RiskGroup> riskGroups,
            int offset,
            int pageSize,
            String productPackageId) {
        log.info("Listing end users for clientAdminId: {}, search: {}, status: {}, departments: {}, riskGroups: {}, "
                        + "offset: {}, pageSize: {}, productPackageId: {}",
                clientAdminId, search, status, departments, riskGroups, offset, pageSize, productPackageId);

        if (!StringUtils.hasText(clientAdminId)) {
            throw new RegistationValidationException("clientAdminId is required");
        }

        int safeOffset = Math.max(offset, 0);
        int safePageSize = pageSize <= 0 ? 10 : pageSize;
        String statusFilter = status != null ? status.name() : null;
        List<UUID> excludeUserIds = resolveExcludedLicensedUserIds(clientAdminId.trim(), productPackageId);

        long total = aspireUserRepository.countEndUsersPaged(
                clientAdminId.trim(), search, statusFilter, departments, riskGroups, excludeUserIds);
        List<AspireUser> page = aspireUserRepository.findEndUsersPaged(
                clientAdminId.trim(), search, statusFilter, departments, riskGroups, excludeUserIds,
                safeOffset, safePageSize);

        ClientAdmin clientAdminForOrganization = clientAdminRepository.findById(clientAdminId.trim()).orElse(null);
        List<EndUserResponseDTO> items = page.stream()
                .map(aspireUserMapper::toDto)
                .map(dto -> convertToEndUserResponseDTO(dto, clientAdminForOrganization))
                .toList();

        return new AllResponseDto<>(safeOffset, safePageSize, total, items);
    }

    private List<UUID> resolveExcludedLicensedUserIds(String clientAdminId, String productPackageId) {
        if (!StringUtils.hasText(productPackageId)) {
            return List.of();
        }
        List<String> licensedIds = phishingServiceClient.getLicensedUserIds(clientAdminId, productPackageId.trim());
        List<UUID> exclude = new ArrayList<>();
        for (String id : licensedIds) {
            if (!StringUtils.hasText(id)) {
                continue;
            }
            try {
                exclude.add(UUID.fromString(id.trim()));
            } catch (IllegalArgumentException e) {
                log.warn("Skipping non-UUID licensed userId for exclusion: {}", id);
            }
        }
        return exclude;
    }


    @Override
    public List<EndUserResponseDTO> importEndUsersFromCsv(MultipartFile file, String clientAdminId) {
        bulkImportFileParser.validateFile(file);
        List<EndUserRequestDTO> endUsers = bulkImportFileParser.parseForLegacyImport(file, clientAdminId);
        List<EndUserResponseDTO> endUserResponses = new ArrayList<>();
        List<BulkImportSummaryDto.ImportedUserInfo> importedUsers = new ArrayList<>();
        List<BulkImportSummaryDto.SkippedUserInfo> skippedUsers = new ArrayList<>();

        for (EndUserRequestDTO endUser : endUsers) {
            Optional<String> skipReason = getSkipReason(endUser);
            if (skipReason.isPresent()) {
                handleSkippedUser(endUser, skipReason.get(), skippedUsers);
                continue;
            }

            try {
                processImportedUser(endUser, clientAdminId, endUserResponses, importedUsers);
            } catch (Exception e) {
                String fullName = endUser.getFirstName() + (endUser.getLastName() != null && !endUser.getLastName().isEmpty() ? " " + endUser.getLastName() : "");
                log.warn("Failed to create user: {} (Name: {}, Phone: {}, Department: {}). Error: {}",
                        endUser.getEmail(), fullName, endUser.getPhoneNumber(), endUser.getDepartment(), e.getMessage());

                skippedUsers.add(BulkImportSummaryDto.SkippedUserInfo.builder()
                        .email(endUser.getEmail())
                        .fullName(fullName)
                        .reason("creation_failed")
                        .build());
            }
        }

        CurrentUserContext context = currentContextService.getCurrentUserContext();
        String adminName = aspireUserService.getUserById(UUID.fromString(context.getUserId()))
                .map(AspireUserDto::getCompanyName)
                .orElse(context.getFullName());

        // Send bulk import summary notification to admin
        try {
            BulkImportSummaryDto summaryDto = BulkImportSummaryDto.builder()
                    .adminEmail(context.getEmail())
                    .adminName(adminName)
                    .adminId(context.getUserId()) // Admin's userId
                    .clientAdminId(context.getClientAdminId() != null ? context.getClientAdminId() : clientAdminId) // Use context clientAdminId or fallback to parameter
                    .totalImported(importedUsers.size())
                    .totalSkipped(skippedUsers.size())
                    .importedUsers(importedUsers)
                    .skippedUsers(skippedUsers)
                    .importDate(LocalDate.now().format(
                            DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                    .build();

            notificationClient.sendBulkImportSummaryNotification(summaryDto);
            log.info("Bulk import summary notification sent to admin: {}", context.getEmail());
        } catch (Exception e) {
            log.error("Failed to send bulk import summary notification: {}", e.getMessage(), e);
        }

        return skippedUsers.stream()
                .map(this::getSkippedUser)
                .toList();
    }

    private EndUserResponseDTO getSkippedUser(BulkImportSummaryDto.SkippedUserInfo skippedUserInfo) {
        if (skippedUserInfo == null) {
            return null;
        }
        return EndUserResponseDTO.builder()
                .email(skippedUserInfo.getEmail())
                .fullName(skippedUserInfo.getFullName())
                .errorReason(skippedUserInfo.getReason())
                .build();
    }

    private Optional<String> getSkipReason(EndUserRequestDTO endUser) {
        String email = endUser.getEmail();

        // Check email domain validation first
        try {
            validateEmailDomain(email, endUser.getClientAdminId());
        } catch (RegistationValidationException | ResourceNotFoundException e) {
            // ResourceNotFoundException can occur if client admin is not found
            // Both exceptions indicate domain validation failure
            return Optional.of("invalid_domain");
        } catch (Exception e) {
            // Catch any other unexpected exceptions during validation
            log.warn("Unexpected error during email domain validation for user {}: {}", email, e.getMessage());
            return Optional.of("invalid_domain");
        }

        if (isExistingUser(email)) {
            return Optional.of("duplicate");
        }

        return Optional.empty();
    }

    private void handleSkippedUser(EndUserRequestDTO endUser, String reason, List<BulkImportSummaryDto.SkippedUserInfo> skippedUsers) {
        String fullName = endUser.getFirstName() + (endUser.getLastName() != null && !endUser.getLastName().isEmpty() ? " " + endUser.getLastName() : "");
        skippedUsers.add(BulkImportSummaryDto.SkippedUserInfo.builder()
                .email(endUser.getEmail())
                .fullName(fullName)
                .reason(reason)
                .build());

        if ("duplicate".equals(reason)) {
            log.info("User already exists: {} (Name: {}, Phone: {}, Department: {})",
                    endUser.getEmail(), fullName, endUser.getPhoneNumber(), endUser.getDepartment());
        } else {
            log.warn("Invalid email domain for user '{}' (Name: {}). User must use the company email to register.",
                    endUser.getEmail(), fullName);
        }
    }

    private void processImportedUser(EndUserRequestDTO endUser,
                                     String clientAdminId,
                                     List<EndUserResponseDTO> endUserResponses,
                                     List<BulkImportSummaryDto.ImportedUserInfo> importedUsers) {
        String email = endUser.getEmail();
        String firstName = endUser.getFirstName();
        String lastName = endUser.getLastName() != null ? endUser.getLastName() : "";
        String fullName = firstName + (lastName.isEmpty() ? "" : " " + lastName);
        String phone = endUser.getPhoneNumber();
        String department = endUser.getDepartment();

        // Create the user - addEndUser already sends welcome email, so we just need the response
        endUser.setStatus(UserStatus.ACTIVE);
        EndUserResponseDTO response = addEndUser(endUser);
        endUserResponses.add(response);

        importedUsers.add(BulkImportSummaryDto.ImportedUserInfo.builder()
                .email(email)
                .fullName(fullName)
                .phoneNumber(phone)
                .department(department)
                .build());

        log.info("Successfully imported user: {}", email);
    }

    private boolean isExistingUser(String email) {
        return aspireUserService.getUserByEmail(email).isPresent();
    }

    @Override
    public void assignSubPackageToUsers(SubPackageAssignRequest subPackageAssignRequest) {
        try {
            // Validate license availability BEFORE assigning any sub-packages
            validateLicenseAvailabilityForBulkAssignment(subPackageAssignRequest);

            // First, create EndUserPackage records in Registration service
            for (SubPackageAssignRequest.SubPackageData subPackageData : subPackageAssignRequest.getSubPackageData()) {
                for (String endUserId : subPackageData.getUserIdList()) {
                    if (endUserPackageRepository.existsByUserIdAndSubPackageId(endUserId, subPackageData.getSubPackageId())) {
                        log.info("Subpackage {} already assigned to user {}", subPackageData.getSubPackageId(), endUserId);
                        continue;
                    }

                    // Build EndUserPackage to calculate expiryDate (but don't save yet)
                    EndUserPackage endUserPackage = buildEndUserPackage(subPackageData, endUserId);

                    // Additional per-user validation as safety check (skipped for simulation products)
                    validateLicenseLimit(subPackageData);

                    // If validation passes, save EndUserPackage
                    endUserPackageRepository.save(endUserPackage);

                    // Simulation seats are consumed at allocate-licence; skip UserLicence / usedLicenseCount here
                    if (!skipsSimulationSeatConsumption(subPackageData.getProductId())) {
                        createUserLicence(subPackageData, endUserId, endUserPackage.getExpiryDate());
                    }

                    log.info("Assigned subpackage {} to user {} with email notification enabled: {}",
                            subPackageData.getProductId(), endUserId, subPackageData.isEnableFirstUserNotificationEmail());

                    // Send notification if enabled
                    if (subPackageData.isEnableFirstUserNotificationEmail()) {
                        sendPackageAssignedNotification(endUserId, subPackageData);
                    }

                    // Send notification to additional recipients (2nd, 3rd, and 4th level emails)
                    // Only call if at least one email list exists and is not empty
                    if (hasValidEmailLists(subPackageData)) {
                        sendPackageAssignedNotificationToAdditionalRecipients(endUserId, subPackageData);
                    }

                }
            }

            // After successfully creating EndUserPackage records, call CMS service
            callCmsServiceForSubPackageAssignment(subPackageAssignRequest);

        } catch (RegistationValidationException e) {
            // Re-throw validation exceptions as-is (will return 400)
            log.error("Validation error assigning subpackage to users: {}", e.getMessage(), e);
            throw e;
        } catch (ResourceNotFoundException e) {
            // Re-throw not found exceptions as-is (will return 404)
            log.error("Resource not found while assigning subpackage to users: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // Only wrap truly unexpected exceptions
            log.error("Unexpected error assigning subpackage to users", e);
            throw new CustomException("Failed to assign subpackage to users: " + e.getMessage(), e);
        }
    }

    /**
     * Send package assignment notification to additional email recipients
     * (secondary, third-level, and fourth HR emails)
     */
    private void sendPackageAssignedNotificationToAdditionalRecipients(String userId, SubPackageAssignRequest.SubPackageData subPackageData) {
        try {
            // Get user details for notification content
            Optional<AspireUserDto> userOpt = aspireUserService.getUserById(UUID.fromString(userId));
            if (userOpt.isEmpty()) {
                log.warn("User not found for additional recipient notification: {}", userId);
                return;
            }

            AspireUserDto user = userOpt.get();
            String userName = user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "");

            CurrentUserContext currentContext = tryGetCurrentContext();

            String effectiveClientAdminId = firstNonBlank(
                    subPackageData.getClientAdminId(),
                    user.getClientAdminId(),
                    currentContext != null ? currentContext.getClientAdminId() : null
            );

            Optional<ClientAdmin> clientAdminOpt = currentContext == null
                    ? findClientAdmin(effectiveClientAdminId)
                    : Optional.empty();

            ClientAdmin clientAdmin = clientAdminOpt.orElse(
                    currentContext != null && effectiveClientAdminId != null
                            ? clientAdminRepository.findById(effectiveClientAdminId).orElse(null)
                            : null
            );

            String adminName = firstNonBlank(
                    currentContext != null ? currentContext.getFullName() : null,
                    clientAdmin != null ? clientAdmin.getOrganizationName() : null
            );

            // Calculate assignment and expiration dates
            LocalDate assignmentDate = LocalDate.now();
            LocalDate expirationDate = calculateExpirationDate(assignmentDate, subPackageData.getCompletionDays());

            // Send to additional recipients
            PackageAssignmentNotificationDto notificationDto = PackageAssignmentNotificationDto.builder()
                    .userName(userName)
                    .packageName(subPackageData.getSubPackageName())
                    .packageDetails(" ")
                    .assignmentDate(assignmentDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                    .expirationDate(expirationDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")))
                    .adminName(adminName)
                    .userId(userId)
                    .clientAdminId(effectiveClientAdminId)
                    .departmentName(user.getDepartment())
                    .companyName(user.getCompanyName())
                    .logoUrl(clientAdmin != null ? clientAdmin.getLogoUrl() : null)
                    .build();

            if(subPackageData.getSecondaryEmails() != null && subPackageData.getSecondaryEmails().size() > 0) {
                notificationClient.sendPackageAssignNotificationForAdditional(notificationDto, subPackageData.getSecondaryEmails(), "Manager" );
            }
            if(subPackageData.getThirdLevelEmails() != null && subPackageData.getThirdLevelEmails().size() > 0) {
                notificationClient.sendPackageAssignNotificationForAdditional(notificationDto, subPackageData.getThirdLevelEmails(), "Sir");
            }
            if(subPackageData.getFourthHREmails() != null && subPackageData.getFourthHREmails().size() > 0) {
                notificationClient.sendPackageAssignNotificationForAdditional(notificationDto, subPackageData.getFourthHREmails(), "HR");
            }

            log.info("Package assignment notification event published successfully to HR, C-Level and Manager");
        } catch (Exception e) {
            log.error("Failed to send package assignment notification to additional recipients for user: {}", userId, e);
        }
    }

    /**
     * Checks if any of the email lists (secondary, third-level, or fourth HR emails) exist and are not empty
     *
     * @param subPackageData the subpackage data containing email lists
     * @return true if at least one email list exists and is not empty, false otherwise
     */
    private boolean hasValidEmailLists(SubPackageAssignRequest.SubPackageData subPackageData) {
        return (subPackageData.getSecondaryEmails() != null && !subPackageData.getSecondaryEmails().isEmpty()) ||
               (subPackageData.getThirdLevelEmails() != null && !subPackageData.getThirdLevelEmails().isEmpty()) ||
               (subPackageData.getFourthHREmails() != null && !subPackageData.getFourthHREmails().isEmpty());
    }

    /**
     * Fetches ClientProduct by productPackageId and validates it matches expected values
     * @param productPackageId the ClientProduct ID (UUID)
     * @param expectedClientAdminId expected clientAdminId for validation
     * @param expectedProductId expected productId for validation
     * @param expectedPackageId expected packageId for validation
     * @param requireActive if true, validates licenseStatus is ACTIVE
     * @return ClientProduct if found and valid
     * @throws ResourceNotFoundException if ClientProduct not found
     * @throws RegistationValidationException if ClientProduct doesn't match expected values or is not ACTIVE
     */
    private ClientProduct fetchAndValidateClientProduct(
            String productPackageId,
            String expectedClientAdminId,
            String expectedProductId,
            String expectedPackageId,
            boolean requireActive
    ) {
        // Fetch ClientProduct by ID
        ClientProduct clientProduct = clientProductRepository.findById(productPackageId)
                .orElse(null);

        if (clientProduct == null) {
            log.error("ClientProduct not found with productPackageId: {}", productPackageId);
            throw new ResourceNotFoundException("ClientProduct not found with productPackageId: " + productPackageId);
        }

        // Validate clientAdminId matches
        if (!Objects.equals(clientProduct.getClientAdminId(), expectedClientAdminId)) {
            log.error("ClientProduct clientAdminId mismatch. Expected: {}, Found: {}, productPackageId: {}",
                    expectedClientAdminId, clientProduct.getClientAdminId(), productPackageId);
            throw new RegistationValidationException(
                    String.format("ClientProduct clientAdminId mismatch. Expected: %s, Found: %s",
                            expectedClientAdminId, clientProduct.getClientAdminId())
            );
        }

        // Validate productId matches
        if (!Objects.equals(clientProduct.getProductId(), expectedProductId)) {
            log.error("ClientProduct productId mismatch. Expected: {}, Found: {}, productPackageId: {}",
                    expectedProductId, clientProduct.getProductId(), productPackageId);
            throw new RegistationValidationException(
                    String.format("ClientProduct productId mismatch. Expected: %s, Found: %s",
                            expectedProductId, clientProduct.getProductId())
            );
        }

        // Validate packageId matches
        if (!Objects.equals(clientProduct.getPackageId(), expectedPackageId)) {
            log.error("ClientProduct packageId mismatch. Expected: {}, Found: {}, productPackageId: {}",
                    expectedPackageId, clientProduct.getPackageId(), productPackageId);
            throw new RegistationValidationException(
                    String.format("ClientProduct packageId mismatch. Expected: %s, Found: %s",
                            expectedPackageId, clientProduct.getPackageId())
            );
        }

        // Validate licenseStatus is ACTIVE if required
        if (requireActive) {
            if (!"ACTIVE".equals(clientProduct.getLicenseStatus())) {
                log.error("ClientProduct is not ACTIVE. LicenseStatus: {}, productPackageId: {}",
                        clientProduct.getLicenseStatus(), productPackageId);
                throw new RegistationValidationException(
                        String.format("Cannot assign license - product is not active. LicenseStatus: %s. Please complete payment first.",
                                clientProduct.getLicenseStatus())
                );
            }
        }

        return clientProduct;
    }

    /**
     * Validates license availability BEFORE assigning sub-packages in bulk.
     * Checks if the number of available licenses is sufficient for all sub-packages being assigned.
     * If insufficient licenses are available, throws an error and prevents any assignments.
     *
     * @param subPackageAssignRequest The request containing all sub-packages to be assigned
     * @throws RegistationValidationException if insufficient licenses are available
     * @throws ResourceNotFoundException if ClientProduct is not found
     */
    private void validateLicenseAvailabilityForBulkAssignment(SubPackageAssignRequest subPackageAssignRequest) {
        if (subPackageAssignRequest.getSubPackageData() == null || subPackageAssignRequest.getSubPackageData().isEmpty()) {
            return; // No sub-packages to assign, skip validation
        }

        for (SubPackageAssignRequest.SubPackageData subPackageData : subPackageAssignRequest.getSubPackageData()) {
            if (skipsSimulationSeatConsumption(subPackageData.getProductId())) {
                log.info("Skipping license capacity check for simulation productId={}",
                        subPackageData.getProductId());
                continue;
            }
            // Fetch and validate ACTIVE ClientProduct using unique productPackageId
            // This prevents "non unique result" errors when duplicate records exist
            ClientProduct clientProduct = fetchAndValidateClientProduct(
                    subPackageData.getProductPackageId(),
                    subPackageData.getClientAdminId(),
                    subPackageData.getProductId(),
                    subPackageData.getPackageId(),
                    true // requireActive = true, will throw error if PENDING
            );

            String clientAdminId = clientProduct.getClientAdminId();
            int licenseCount = clientProduct.getLicenseCount();

            // Count existing licences for this specific ClientProduct
            long existingLicenceCount = userLicenceRepository.countByClientAdminIdAndProductIdAndPackageId(
                    clientAdminId,
                    subPackageData.getProductId(),
                    subPackageData.getPackageId()
            );

            // Count how many NEW users will be assigned (excluding those who already have the sub-package)
            long newAssignmentsCount = 0;
            if (subPackageData.getUserIdList() != null) {
                for (String endUserId : subPackageData.getUserIdList()) {
                    // Only count users who don't already have this sub-package assigned
                    if (!endUserPackageRepository.existsByUserIdAndSubPackageId(endUserId, subPackageData.getSubPackageId())) {
                        newAssignmentsCount++;
                    }
                }
            }

            // Calculate available licenses
            long availableLicenses = licenseCount - existingLicenceCount;

            // Check if available licenses are sufficient for new assignments
            if (availableLicenses < newAssignmentsCount) {
                log.error("Insufficient licenses for bulk assignment. ClientAdminId: {}, productId: {}, packageId: {}. " +
                        "Available licenses: {}, Required licenses: {}, Existing licenses: {}, License limit: {}",
                        clientAdminId, subPackageData.getProductId(), subPackageData.getPackageId(),
                        availableLicenses, newAssignmentsCount, existingLicenceCount, licenseCount);
                throw new RegistationValidationException(
                        String.format("Insufficient licenses available. Cannot assign sub-package to %d user(s). " +
                                "Available licenses: %d, Required licenses: %d, License limit: %d, Currently used: %d",
                                newAssignmentsCount, availableLicenses, newAssignmentsCount, licenseCount, existingLicenceCount)
                );
            }

            log.info("License validation passed for bulk assignment. ClientAdminId: {}, productId: {}, packageId: {}. " +
                    "Available licenses: {}, Required licenses: {}, License limit: {}",
                    clientAdminId, subPackageData.getProductId(), subPackageData.getPackageId(),
                    availableLicenses, newAssignmentsCount, licenseCount);
        }
    }

    /**
     * Validates license limit for a single user assignment (per-user safety check).
     * This method is called as an additional validation after bulk validation.
     * Validates license limit before creating UserLicence.
     * This method should be called BEFORE saving EndUserPackage to prevent orphaned records.
     * Only ACTIVE products can have licenses assigned - PENDING products are not yet paid for.
     */
    private void validateLicenseLimit(SubPackageAssignRequest.SubPackageData subPackageData) {
        if (skipsSimulationSeatConsumption(subPackageData.getProductId())) {
            return;
        }
        // Fetch and validate ACTIVE ClientProduct using productPackageId
        ClientProduct clientProduct = fetchAndValidateClientProduct(
                subPackageData.getProductPackageId(),
                subPackageData.getClientAdminId(),
                subPackageData.getProductId(),
                subPackageData.getPackageId(),
                true
        );

        String clientAdminId = clientProduct.getClientAdminId();
        int licenseCount = clientProduct.getLicenseCount();

        // Count existing licences for this specific ClientProduct (clientAdminId + productId + packageId combination)
        // This ensures license limits are enforced per product-package combination, not just per package
        long existingLicenceCount = userLicenceRepository.countByClientAdminIdAndProductIdAndPackageId(
                clientAdminId,
                subPackageData.getProductId(),
                subPackageData.getPackageId()
        );

        // Check if adding this licence would exceed the license limit
        if (existingLicenceCount >= licenseCount) {
            long remaining = licenseCount - existingLicenceCount;
            log.error("License limit exceeded for clientAdminId: {}, productId: {} and packageId: {}. " +
                      "Existing: {}, Limit: {}, Remaining: {}",
                      clientAdminId, subPackageData.getProductId(), subPackageData.getPackageId(),
                      existingLicenceCount, licenseCount, remaining);
            throw new RegistationValidationException(
                String.format("License limit exceeded for product '%s' and package '%s'. " +
                             "Current licenses: %d, License limit: %d, Remaining: %d",
                             subPackageData.getProductId(), subPackageData.getPackageId(),
                             existingLicenceCount, licenseCount, remaining)
            );
        }
    }

    /**
     * Phishing / Smishing / Vishing Simulation seats are consumed at allocate-licence
     * ({@code phishing_user_licence} + {@code ClientProduct.usedLicenseCount}). Training
     * enrollment via assign-sub-package must not create a second seat or overwrite used count.
     */
    private boolean skipsSimulationSeatConsumption(String productId) {
        return simulationProductResolver.isSimulationProduct(productId);
    }

    /**
     * Creates UserLicence record for the assigned subpackage
     * Note: License validation should be performed before calling this method
     * Only ACTIVE products can have licenses created - PENDING products are not yet paid for
     *
     * @param expiryDate the expiry date from the EndUserPackage (already calculated)
     */
    private UserLicence createUserLicence(SubPackageAssignRequest.SubPackageData subPackageData, String endUserId, Instant expiryDate) {
        try {
            // Check if licence already exists for this user and package combination
            if (userLicenceRepository.existsByUserIdAndPackageId(endUserId, subPackageData.getPackageId())) {
                log.info("Licence already exists for user {} and package {}", endUserId, subPackageData.getPackageId());
                return null; // Return null if already exists
            }

            // Fetch and validate ACTIVE ClientProduct using productPackageId
            ClientProduct clientProduct = fetchAndValidateClientProduct(
                    subPackageData.getProductPackageId(),
                    subPackageData.getClientAdminId(),
                    subPackageData.getProductId(),
                    subPackageData.getPackageId(),
                    true
            );

            String clientAdminId = clientProduct.getClientAdminId();

            // Count existing licences for this specific ClientProduct (clientAdminId + productId + packageId combination)
            // Note: License validation was already performed, but we need the count to update usedLicenseCount
            long existingLicenceCount = userLicenceRepository.countByClientAdminIdAndProductIdAndPackageId(
                    clientAdminId,
                    subPackageData.getProductId(),
                    subPackageData.getPackageId()
            );

            // Create UserLicence record with current time for issueDate and provided expiryDate
            UserLicence userLicence = UserLicence.builder()
                    .id(UUID.randomUUID().toString())
                    .userId(endUserId)
                    .clientAdminId(clientAdminId)
                    .productId(subPackageData.getProductId())
                    .packageId(subPackageData.getPackageId())
                    .subPackageId(subPackageData.getSubPackageId())
                    .licenceStatus(LicenceStatus.ASSIGNED)
                    .issueDate(Instant.now())
                    .expireDate(expiryDate) // Use provided expiryDate from EndUserPackage
                    .build();

            UserLicence savedUserLicence = userLicenceRepository.save(userLicence);

            // Update ClientProduct usedLicenseCount
            int newUsedLicenseCount = (int) (existingLicenceCount + 1);
            clientProduct.setUsedLicenseCount(newUsedLicenseCount);
            clientProductRepository.save(clientProduct);

            log.info("Created UserLicence for user {} and package {} with clientAdminId {}. Updated usedLicenseCount to {}",
                    endUserId, subPackageData.getPackageId(), clientAdminId, newUsedLicenseCount);

            return savedUserLicence; // Return the created UserLicence

        } catch (Exception e) {
            log.error("Error creating UserLicence for user {} and package {}: {}",
                    endUserId, subPackageData.getPackageId(), e.getMessage(), e);
            throw new CustomException("Failed to create UserLicence", e);
        }
    }

    /**
     * Calls CMS service to create user progress tracking records
     */
    private void callCmsServiceForSubPackageAssignment(SubPackageAssignRequest subPackageAssignRequest) {
        try {
            log.info("Calling CMS service to create user progress tracking records");

            // Convert Registration service request to CMS service request format
            CmsUserSubPackageAssignRequest cmsRequest = CmsUserSubPackageAssignRequest.fromRegistrationRequest(subPackageAssignRequest);

            // Call CMS service endpoint
            webClient.post()
                    .uri(cmsServiceUrl + "/user-subpackages/assign")
                    .bodyValue(cmsRequest)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

            log.info("Successfully called CMS service for subpackage assignment");

        } catch (WebClientResponseException e) {
            log.error("CMS service returned error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            // Don't throw exception here to avoid rolling back the EndUserPackage creation
            // The user assignment in Registration service should still succeed
        } catch (Exception e) {
            log.error("Failed to call CMS service for subpackage assignment", e);
            // Don't throw exception here to avoid rolling back the EndUserPackage creation
            // The user assignment in Registration service should still succeed
        }
    }


    private EndUserPackage buildEndUserPackage(SubPackageAssignRequest.SubPackageData subPackageData, String endUserId) {
        EndUserPackage endUserPackage = new EndUserPackage();
        endUserPackage.setId(UUID.randomUUID().toString());
        endUserPackage.setUserId(endUserId);
        endUserPackage.setClientAdminId(subPackageData.getClientAdminId());
        endUserPackage.setProductId(subPackageData.getProductId());
        endUserPackage.setSubPackageId(subPackageData.getSubPackageId());
        endUserPackage.setStatus(PackageStatus.ASSIGNED.name());
        endUserPackage.setProgress(0.0);
        endUserPackage.setAssignedAt(Instant.now());
        endUserPackage.setExpiryDate(calculateAndVerifyExpiryDate(subPackageData));
        endUserPackage.setActive(Boolean.TRUE);
        endUserPackage.setEnableFirstUserNotificationEmail(subPackageData.isEnableFirstUserNotificationEmail());

        if (subPackageData.isEnableFirstUserNotificationEmail()) {
            endUserPackage.setSecondaryEmails(
                    subPackageData.getSecondaryEmails() != null ? subPackageData.getSecondaryEmails() : List.of());
            endUserPackage.setThirdLevelEmails(
                    subPackageData.getThirdLevelEmails() != null ? subPackageData.getThirdLevelEmails() : List.of());
            endUserPackage.setFourthHREmails(
                    subPackageData.getFourthHREmails() != null ? subPackageData.getFourthHREmails() : List.of());

            if (subPackageData.getCompletionDays() != null) {
                EndUserPackage.CompletionDays completionDays = EndUserPackage.CompletionDays.builder()
                        .durationUnit(EndUserPackage.DurationUnit.valueOf(subPackageData.getCompletionDays().getDurationUnit().name()))
                        .durationValue(subPackageData.getCompletionDays().getDurationValue())
                        .build();
                endUserPackage.setCompletionDays(completionDays);
            }
        }
        return endUserPackage;
    }

    private Instant calculateAndVerifyExpiryDate(SubPackageAssignRequest.SubPackageData subPackageData) {

        // Step 1: Calculate expiry (still using Instant for storage)
        Instant subPackageExpiryDate = Instant.now()
                .plusSeconds(subPackageData.getValidFor() * 24L * 60 * 60);

        try {
            ClientProduct clientProduct = fetchAndValidateClientProduct(
                    subPackageData.getProductPackageId(),
                    subPackageData.getClientAdminId(),
                    subPackageData.getProductId(),
                    subPackageData.getPackageId(),
                    true
            );

            Instant productExpiryDate = clientProduct.getExpiryDate();

            if (productExpiryDate != null) {

                // Step 2: Convert both to LocalDate (ignore time)
                ZoneId zone = ZoneId.systemDefault();

                LocalDate subPackageDate = subPackageExpiryDate.atZone(zone).toLocalDate();
                LocalDate productDate = productExpiryDate.atZone(zone).toLocalDate();

                // Step 3: Compare only date
                if (subPackageDate.isAfter(productDate)) {
                    throw new RegistationValidationException(
                            "The assigned duration exceeds the package validity period."
                    );
                }
            }

        } catch (ResourceNotFoundException e) {
            log.warn("ClientProduct not found for expiry validation, skipping check. productPackageId: {}",
                    subPackageData.getProductPackageId());
            return subPackageExpiryDate;
        }

        return subPackageExpiryDate;
    }

    @Override
    public List<EndUserResponseDTO> getUnassignedUsersForProduct(
            String clientAdminId,
            String subPackageId,
            String search,
            UserStatus status,
            List<String> departments,
            RiskGroup riskGroup,
            int offset,
            int pageSize) {
        log.info("Getting unassigned users for product: {} by clientAdminId: {}", subPackageId, clientAdminId);

        try {
            // Get all users with UserType.USER created by the specific client admin
            List<AspireUserDto> aspireUsers = aspireUserService.getUsersByTypeAndClientAdminId(UserType.USER.name(), clientAdminId);

            // Get users already assigned to this product
            List<String> assignedUserIds = endUserPackageRepository.findBySubPackageId(subPackageId)
                    .stream()
                    .map(EndUserPackage::getUserId)
                    .toList();

            // Filter users who are not assigned to the product
            List<AspireUserDto> unassignedUsers = aspireUsers.stream()
                    .filter(user -> !assignedUserIds.contains(user.getBaseUserId().toString()))
                    .filter(user -> status == null || user.getStatus().equals(status.name()))
                    .filter(user -> departments == null || departments.isEmpty() ||
                            (user.getDepartment() != null && departments.stream()
                                    .anyMatch(dept -> dept != null && dept.equalsIgnoreCase(user.getDepartment()))))
                    .filter(user -> riskGroup == null || user.getRiskGroup() == riskGroup)
                    .filter(user -> search == null || search.isEmpty() ||
                            (user.getEmail() != null && user.getEmail().toLowerCase().contains(search.toLowerCase())) ||
                            (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(search.toLowerCase())) ||
                            (user.getLastName() != null && user.getLastName().toLowerCase().contains(search.toLowerCase())) ||
                            (user.getFirstName() + " " + user.getLastName()).toLowerCase().contains(search.toLowerCase()))
                    .sorted((user1, user2) -> {
                        // Sort by createdAt in descending order (newest first)
                        if (user1.getCreatedAt() == null && user2.getCreatedAt() == null) return 0;
                        if (user1.getCreatedAt() == null) return 1;
                        if (user2.getCreatedAt() == null) return -1;
                        return user2.getCreatedAt().compareTo(user1.getCreatedAt());
                    })
                    .skip(offset == 0 ? 0 : ((long) offset * pageSize))
                    .limit(Math.max(1, pageSize))
                    .toList();

            // Convert to EndUserResponseDTO
            final ClientAdmin clientAdminForOrganization =
                    (clientAdminId != null && !clientAdminId.isBlank())
                            ? clientAdminRepository.findById(clientAdminId).orElse(null)
                            : null;
            return unassignedUsers.stream()
                    .map(user -> convertToEndUserResponseDTO(user, clientAdminForOrganization))
                    .toList();

        } catch (Exception e) {
            log.error("Error getting unassigned users for product: {} by clientAdminId: {}", subPackageId, clientAdminId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public long countUnassignedUsersForProduct(
            String clientAdminId,
            String subPackageId,
            String search,
            UserStatus status,
            List<String> departments,
            RiskGroup riskGroup) {
        log.info("Counting unassigned users for product: {} by clientAdminId: {}", subPackageId, clientAdminId);

        try {
            // Get all users with UserType.USER created by the specific client admin
            List<AspireUserDto> aspireUsers = aspireUserService.getUsersByTypeAndClientAdminId(UserType.USER.name(), clientAdminId);

            // Get users already assigned to this product
            List<String> assignedUserIds = endUserPackageRepository.findBySubPackageId(subPackageId)
                    .stream()
                    .map(EndUserPackage::getUserId)
                    .toList();

            // Count users who are not assigned to the product
            return aspireUsers.stream()
                    .filter(user -> !assignedUserIds.contains(user.getBaseUserId().toString()))
                    .filter(user -> status == null || user.getStatus().equals(status.name()))
                    .filter(user -> departments == null || departments.isEmpty() ||
                            (user.getDepartment() != null && departments.stream()
                                    .anyMatch(dept -> dept != null && dept.equalsIgnoreCase(user.getDepartment()))))
                    .filter(user -> riskGroup == null || user.getRiskGroup() == riskGroup)
                    .filter(user -> search == null || search.isEmpty() ||
                            (user.getEmail() != null && user.getEmail().toLowerCase().contains(search.toLowerCase())) ||
                            (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(search.toLowerCase())) ||
                            (user.getLastName() != null && user.getLastName().toLowerCase().contains(search.toLowerCase())) ||
                            (user.getFirstName() + " " + user.getLastName()).toLowerCase().contains(search.toLowerCase()))
                    .count();

        } catch (Exception e) {
            log.error("Error counting unassigned users for product: {} by clientAdminId: {}", subPackageId, clientAdminId, e);
            return 0;
        }
    }

    @Override
    public boolean userExistsByEmail(String email) {
        return aspireUserService.getUserByEmail(email).isPresent();
    }

    private EndUserResponseDTO convertToEndUserResponseDTO(AspireUserDto aspireUser) {
        return convertToEndUserResponseDTO(aspireUser, null);
    }

    /**
     * Converts an end user to response DTO. When {@code clientAdminForOrganization} is non-null
     * (e.g. after a single {@code findById} for a list endpoint), organization fields are taken from it;
     * otherwise they are loaded from {@link ClientAdminRepository#findById(String)} using the user's clientAdminId.
     */
    private EndUserResponseDTO convertToEndUserResponseDTO(AspireUserDto aspireUser, ClientAdmin clientAdminForOrganization) {
        String firstName = aspireUser.getFirstName() != null ? aspireUser.getFirstName() : "";
        String lastName = aspireUser.getLastName() != null ? aspireUser.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();

        // Safely convert status string to UserStatus enum
        UserStatus status = null;
        if (aspireUser.getStatus() != null && !aspireUser.getStatus().trim().isEmpty()) {
            try {
                status = UserStatus.fromString(aspireUser.getStatus());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status value '{}' for user {}, defaulting to null",
                        aspireUser.getStatus(), aspireUser.getEmail());
            }
        }

        EndUserResponseDTO.EndUserResponseDTOBuilder builder = EndUserResponseDTO.builder()
                .id(aspireUser.getBaseUserId().toString())
                .firstName(firstName)
                .lastName(lastName)
                .fullName(fullName.isEmpty() ? null : fullName)
                .email(aspireUser.getEmail())
                .phoneNumber(aspireUser.getPhoneNumber())
                .phoneCode(aspireUser.getPhoneCode())
                .department(aspireUser.getDepartment())
                .countryName(aspireUser.getCountry())
                .countryCode(aspireUser.getCountryCode())
                .status(status)
                .clientAdminId(aspireUser.getClientAdminId())
                .riskGroup(aspireUser.getRiskGroup())
                .lastLoginAt(aspireUser.getLastLoginAt())
                .profilePicture(aspireUser.getProfilePicture())
                .isRiskProfileExist(aspireUser.getIsRiskProfileExist() != null ? aspireUser.getIsRiskProfileExist() : false);

        applyOrganizationDetails(builder, clientAdminForOrganization, aspireUser.getClientAdminId());
        return builder.build();
    }

    private void applyOrganizationDetails(EndUserResponseDTO.EndUserResponseDTOBuilder builder,
                                            ClientAdmin clientAdminForOrganization,
                                            String clientAdminId) {
        if (clientAdminForOrganization != null) {
            builder.organizationName(clientAdminForOrganization.getOrganizationName());
            builder.organizationDomain(clientAdminForOrganization.getDomain());
            return;
        }
    }


    @Override
    public List<UserSubPackageResponseDTO> getUserAssignedSubPackages(String userId, String status) {
        log.info("Retrieving sub-packages for user: {} with status filter: {}", userId, status);

        try {
            List<EndUserPackage> userPackages;

            if (status != null && !status.trim().isEmpty()) {
                // Filter by specific status
                userPackages = endUserPackageRepository.findByUserIdAndStatusAndActiveTrue(userId, status);
                log.info("Filtering by status: {}", status);
            } else {
                // Return all active packages (existing behavior)
                userPackages = endUserPackageRepository.findByUserIdAndActiveTrue(userId);
                log.info("Returning all active packages");
            }

            if (userPackages.isEmpty()) {
                log.info("No sub-packages found for user: {} with status filter: {}", userId, status);
                return List.of();
            }

            List<UserSubPackageResponseDTO> responseDTOs = userPackages.stream()
                    .map(this::convertToUserSubPackageResponseDTO)
                    .toList();

            log.info("Successfully retrieved {} sub-packages for user: {} with status filter: {}",
                    responseDTOs.size(), userId, status);
            return responseDTOs;

        } catch (Exception e) {
            log.error("Error retrieving sub-packages for user: {} with status filter: {}", userId, status, e);
            throw new ResourceNotFoundException("Failed to retrieve user sub-packages");
        }
    }

    /**
     * Convert EndUserPackage entity to UserSubPackageResponseDTO
     */
    private UserSubPackageResponseDTO convertToUserSubPackageResponseDTO(EndUserPackage endUserPackage) {
        return UserSubPackageResponseDTO.builder()
                .id(endUserPackage.getId())
                .userId(endUserPackage.getUserId())
                .productId(endUserPackage.getProductId())
                .subPackageId(endUserPackage.getSubPackageId())
                .progress(endUserPackage.getProgress())
                .status(endUserPackage.getStatus())
                .assignedAt(endUserPackage.getAssignedAt())
                .expiryDate(endUserPackage.getExpiryDate())
                .build();
    }

    /**
     * Send package assignment notification to user and admin
     */
    private void sendPackageAssignedNotification(String userId, SubPackageAssignRequest.SubPackageData subPackageData) {
        try {
            // Fetch AspireUser entity (not DTO) to check password and update if needed
            Optional<AspireUser> aspireUserOpt = aspireUserRepository.findByUserId(UUID.fromString(userId));
            if (aspireUserOpt.isEmpty()) {
                log.warn("User not found for notification: {}", userId);
                return;
            }

            AspireUser aspireUser = aspireUserOpt.get();
            String userEmail = aspireUser.getUsername();
            String userName = (aspireUser.getFirstName() != null ? aspireUser.getFirstName() : "") + 
                             (aspireUser.getLastName() != null ? " " + aspireUser.getLastName() : "");

            log.info("*** start Current context for notification not found, defaulting to user context. userId: {}", userId);
            CurrentUserContext currentContext = tryGetCurrentContext();
            log.info("***after Current context for notification not found, defaulting to user context. userId: {}", userId);

            String effectiveClientAdminId = firstNonBlank(
                    subPackageData.getClientAdminId(),
                    aspireUser.getClientAdminId(),
                    currentContext != null ? currentContext.getClientAdminId() : null
            );
            Optional<ClientAdmin> clientAdminOpt = currentContext == null
                    ? findClientAdmin(effectiveClientAdminId)
                    : Optional.empty();

            String adminEmail = firstNonBlank(
                    currentContext != null ? currentContext.getEmail() : null,
                    clientAdminOpt.map(ClientAdmin::getEmail).orElse(null),
                    clientAdminOpt.map(ClientAdmin::getContactEmail).orElse(null)
            );
            String adminName = firstNonBlank(
                    currentContext != null ? currentContext.getFullName() : null,
                    clientAdminOpt.map(ClientAdmin::getOrganizationName).orElse(null)
            );
            String adminId = firstNonBlank(
                    effectiveClientAdminId,
                    currentContext != null ? currentContext.getUserId() : null
            );

            String packageName = subPackageData.getSubPackageName();
            String packageDetails = " ";
            String tempPassword = null;
            boolean isSendCredentials = false;

            // Check if password is null
            if ((aspireUser.getPassword() == null || aspireUser.getPassword().trim().isEmpty()) || (aspireUser.getIsCredentialSent() != null && !aspireUser.getIsCredentialSent())) {
                log.info("Password is null for user: {}, generating temporary password", userId);
                isSendCredentials = true;
                
                // Generate temporary password
                tempPassword = generateRandomPassword();
                
                // Hash the temporary password
                String hashedPassword = passwordEncoder.encode(tempPassword);
                
                // Update AspireUser
                aspireUser.setPassword(hashedPassword);
                aspireUser.setIsDefault(true);
                aspireUser.setTempPasswordExpiry(Instant.now().plusSeconds(8 * 60 * 60)); // 8 hours from now
                aspireUser.setIsCredentialSent(true);
                
                log.info("Generated temporary password for user: {} and set isDefault=true, tempPasswordExpiry={}", 
                        userId, aspireUser.getTempPasswordExpiry());
            } 

            // Format assignment date
            String assignmentDate = java.time.LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy"));

            // Build notification DTO
            PackageAssignmentNotificationDto notificationDto = PackageAssignmentNotificationDto.builder()
                    .userEmail(userEmail)
                    .userId(userId)
                    .userName(userName)
                    .packageName(packageName)
                    .packageDetails(packageDetails)
                    .assignmentDate(assignmentDate)
                    .adminEmail(adminEmail)
                    .adminName(adminName)
                    .adminId(adminId)
                    .clientAdminId(effectiveClientAdminId) // clientAdminId for client-specific notification settings
                    .tempPassword(tempPassword) // temporary password (plain text for email)
                    .build();

            // Send notification
            notificationClient.sendPackageAssignedNotification(notificationDto, isSendCredentials);

            // Update AspireUser after sending notification (if password was updated)
            if (isSendCredentials) {
                aspireUser.setIsRiskProfileExist(true);
                aspireUserRepository.save(aspireUser);
                log.info("Updated AspireUser with temporary password for userId: {}", userId);
                aspireUserService.saveUserRiskProfileInPhishing(aspireUser);
                log.info("User risk profile saved in Phishing for userId: {}", userId);
            }

            log.info("Package assignment notification event published successfully to user: {} and admin: {}", userEmail, adminEmail);

        } catch (Exception e) {
            log.error("Error sending package assignment notification for userId: {}, subPackageId: {}",
                    userId, subPackageData.getSubPackageId(), e);
        }
    }

    private CurrentUserContext tryGetCurrentContext() {
        try {
            return currentContextService.getCurrentUserContext();
        } catch (Exception ex) {
            log.debug("CurrentContext unavailable for system-triggered assignment: {}", ex.getMessage());
            return null;
        }
    }

    private Optional<ClientAdmin> findClientAdmin(String clientAdminId) {
        if (clientAdminId == null || clientAdminId.isBlank()) {
            return Optional.empty();
        }
        return clientAdminRepository.findById(clientAdminId);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    @Override
    public Optional<UserDataDto> getUserDataById(String userId) {
        log.info("Getting user data by ID: {}", userId);

        try {
            Optional<AspireUser> aspireUserOpt = aspireUserRepository.findByUserId(UUID.fromString(userId));

            if (aspireUserOpt.isEmpty()) {
                log.warn("User not found with ID: {}", userId);
                return Optional.empty();
            }

            AspireUser aspireUser = aspireUserOpt.get();

            // Build UserDataDto with user information
            UserDataDto.UserDataDtoBuilder builder = UserDataDto.builder()
                    .userId(aspireUser.getUserId().toString())
                    .firstName(aspireUser.getFirstName())
                    .lastName(aspireUser.getLastName())
                    .email(aspireUser.getEmail())
                    .phone(aspireUser.getPhoneNumber())
                    .clientAdminId(aspireUser.getClientAdminId());

            // If clientAdminId exists, fetch client admin information
            if (aspireUser.getClientAdminId() != null && !aspireUser.getClientAdminId().trim().isEmpty()) {
                Optional<ClientAdmin> clientAdminOpt = clientAdminRepository.findById(aspireUser.getClientAdminId());
                if (clientAdminOpt.isPresent()) {
                    ClientAdmin clientAdmin = clientAdminOpt.get();
                    builder.clientAdminName(clientAdmin.getOrganizationName())
                            .clientAdminEmail(clientAdmin.getEmail());
                } else {
                    log.warn("Client admin not found with ID: {}", aspireUser.getClientAdminId());
                }
            }

            UserDataDto userData = builder.build();
            log.info("Successfully retrieved user data for userId: {}", userId);
            return Optional.of(userData);

        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for userId: {}", userId, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting user data by ID: {}", userId, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<NotificationRecipientBundleDto> getNotificationRecipientBundle(String userId) {
        log.info("Building notification recipient bundle for userId: {}", userId);

        try {
            Optional<AspireUser> aspireUserOpt = aspireUserRepository.findByUserId(UUID.fromString(userId));
            if (aspireUserOpt.isEmpty()) {
                log.warn("User not found with ID: {}", userId);
                return Optional.empty();
            }

            AspireUser aspireUser = aspireUserOpt.get();

            NotificationRecipientBundleDto.NotificationRecipientBundleDtoBuilder builder = NotificationRecipientBundleDto.builder()
                    .userId(aspireUser.getUserId().toString())
                    .userFullName(joinName(aspireUser.getFirstName(), aspireUser.getLastName()))
                    .email(aspireUser.getEmail())
                    .phoneNumber(aspireUser.getPhoneNumber())
                    .phoneCode(aspireUser.getPhoneCode())
                    .clientAdminId(aspireUser.getClientAdminId());

            String clientAdminId = aspireUser.getClientAdminId();
            if (clientAdminId != null && !clientAdminId.trim().isEmpty()) {
                Optional<ClientAdmin> clientAdminOpt = clientAdminRepository.findById(clientAdminId);
                if (clientAdminOpt.isPresent()) {
                    ClientAdmin clientAdmin = clientAdminOpt.get();
                    builder.clientAdminName(firstNonBlank(clientAdmin.getOrganizationName(), clientAdmin.getBillingName()))
                            .clientAdminEmail(firstNonBlank(clientAdmin.getEmail(), clientAdmin.getContactEmail()))
                            .clientAdminPhoneNumber(clientAdmin.getPhoneNumber())
                            .clientAdminPhoneCode(clientAdmin.getPhoneCode())
                            .organizationName(clientAdmin.getOrganizationName())
                            .mspId(clientAdmin.getMspId());

                    populateOrganizationTimezone(builder, clientAdmin.getTimeZone());

                    aspireUserRepository.findByUserTypeAndClientAdminId(UserType.CLIENT_ADMIN.name(), clientAdminId)
                            .stream()
                            .findFirst()
                            .ifPresent(clientAdminUser -> builder.clientAdminUserId(clientAdminUser.getUserId().toString()));

                    populateMspInfo(builder, clientAdmin.getMspId());
                } else {
                    log.warn("Client admin not found with ID: {}", clientAdminId);
                }
            }

            builder.aspireAdmins(collectActiveAspireAdmins());

            return Optional.of(builder.build());
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format for userId: {}", userId, e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error building notification recipient bundle for userId: {}", userId, e);
            return Optional.empty();
        }
    }

    private void populateOrganizationTimezone(
            NotificationRecipientBundleDto.NotificationRecipientBundleDtoBuilder builder,
            String timezoneRef) {
        if (timezoneRef == null || timezoneRef.trim().isEmpty()) {
            return;
        }
        builder.organizationTimezoneRef(timezoneRef.trim());
        timezoneRepository.findById(timezoneRef.trim()).ifPresentOrElse(timezone -> {
            builder.organizationTimezoneLabel(timezone.getTimezoneId())
                    .organizationTimezoneDisplayName(timezone.getDisplayName());
        }, () -> {
            // Legacy rows may store an IANA id or offset label directly on client_admins.timeZone
            builder.organizationTimezoneLabel(timezoneRef.trim());
            log.debug("Timezone document not found for ref {}; treating value as timezone label", timezoneRef);
        });
    }

    private void populateMspInfo(NotificationRecipientBundleDto.NotificationRecipientBundleDtoBuilder builder, String mspId) {
        if (mspId == null || mspId.trim().isEmpty()) {
            return;
        }
        Optional<MspUser> mspUserOpt = mspUsersRepository.findById(mspId);
        if (mspUserOpt.isEmpty()) {
            log.warn("MSP not found with ID: {}", mspId);
            return;
        }
        MspUser mspUser = mspUserOpt.get();
        builder.mspName(mspUser.getOrganizationName())
                .mspEmail(firstNonBlank(mspUser.getMspAdminEmail(), mspUser.getContactEmail()))
                .mspPhoneNumber(mspUser.getPhoneNumber())
                .mspPhoneCode(mspUser.getPhoneCode());

        try {
            aspireUserRepository.findByUserId(UUID.fromString(mspUser.getId()))
                    .ifPresent(mspLoginUser -> builder.mspUserId(mspLoginUser.getUserId().toString()));
        } catch (IllegalArgumentException ex) {
            log.debug("MSP id {} is not a UUID, skipping MSP in-app identity lookup", mspUser.getId());
        }
    }

    private List<NotificationRecipientContactDto> collectActiveAspireAdmins() {
        List<NotificationRecipientContactDto> aspireAdmins = new ArrayList<>();
        for (String adminUserType : List.of(UserType.ASPIRE_ADMIN.name())) {
            aspireUserRepository.findByUserType(adminUserType).stream()
                    .filter(admin -> "ACTIVE".equalsIgnoreCase(admin.getStatus()))
                    .forEach(admin -> aspireAdmins.add(NotificationRecipientContactDto.builder()
                            .userId(admin.getUserId() != null ? admin.getUserId().toString() : null)
                            .name(joinName(admin.getFirstName(), admin.getLastName()))
                            .email(admin.getEmail())
                            .phoneNumber(admin.getPhoneNumber())
                            .phoneCode(admin.getPhoneCode())
                            .build()));
        }
        return aspireAdmins;
    }

    private static String joinName(String firstName, String lastName) {
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";
        String joined = (first + " " + last).trim();
        return joined.isEmpty() ? null : joined;
    }

    /**
     * Validates that the end user's email domain matches the client admin's company email domain
     * @param endUserEmail The email address of the end user to validate
     * @param clientAdminId The ID of the client admin
     * @throws RegistationValidationException if domains don't match or validation fails
     * @throws ResourceNotFoundException if client admin is not found
     */
    private void validateEmailDomain(String endUserEmail, String clientAdminId) {
        // Trim and validate input parameters
        if (endUserEmail == null || endUserEmail.trim().isEmpty()) {
            throw new RegistationValidationException("Email address is required");
        }
        endUserEmail = endUserEmail.trim();

        if (clientAdminId == null || clientAdminId.trim().isEmpty()) {
            throw new RegistationValidationException("Client admin ID is required");
        }
        clientAdminId = clientAdminId.trim();

        try {
            // Get client admin user from aspire_user table
            Optional<AspireUserDto> clientAdminOpt = aspireUserService.getUserById(UUID.fromString(clientAdminId));

            if (clientAdminOpt.isEmpty()) {
                throw new ResourceNotFoundException("Client admin not found with ID: " + clientAdminId);
            }

            AspireUserDto clientAdmin = clientAdminOpt.get();
            String clientAdminEmail = clientAdmin.getEmail();

            if (clientAdminEmail == null || clientAdminEmail.trim().isEmpty()) {
                throw new RegistationValidationException("Client admin email not found");
            }

            // Extract domains from both emails (trimming is handled inside extractDomainFromEmail)
            String clientAdminDomain = extractDomainFromEmail(clientAdminEmail.trim());
            String endUserDomain = extractDomainFromEmail(endUserEmail);

            // Compare domains (case-insensitive)
            if (!clientAdminDomain.equalsIgnoreCase(endUserDomain)) {
                log.warn("Email domain mismatch for end user: {} (domain: {}) vs client admin: {} (domain: {})",
                        endUserEmail, endUserDomain, clientAdminEmail, clientAdminDomain);
                throw new RegistationValidationException(MessageKeys.USER_COMPANY_EMAIL_REQUIRED);
            }

            log.debug("Email domain validation passed for end user: {} (domain: {})", endUserEmail, endUserDomain);

        } catch (IllegalArgumentException e) {
            log.error("Invalid client admin ID format: {}", clientAdminId, e);
            throw new RegistationValidationException("Invalid client admin ID format");
        }
    }

    /**
     * Extracts the domain from an email address
     * @param email The email address
     * @return The domain part of the email (e.g., "company.com" from "user@company.com")
     * @throws RegistationValidationException if email format is invalid
     */
    private String extractDomainFromEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new RegistationValidationException("Email address cannot be empty");
        }

        int atIndex = email.indexOf('@');
        if (atIndex == -1 || atIndex == email.length() - 1) {
            throw new RegistationValidationException("Invalid email format: " + email);
        }

        String domain = email.substring(atIndex + 1).trim();
        if (domain.isEmpty()) {
            throw new RegistationValidationException("Invalid email format: domain is empty");
        }

        return domain.toLowerCase();
    }

    /**
     * Calculates the expiration date based on assignment date and completion days configuration
     * @param assignmentDate The date when the package was assigned
     * @param completionDays The completion days configuration containing duration unit and value
     * @return The calculated expiration date
     */
    private LocalDate calculateExpirationDate(LocalDate assignmentDate, SubPackageAssignRequest.CompletionDays completionDays) {
        if (completionDays == null || completionDays.getDurationValue() == null || completionDays.getDurationUnit() == null) {
            // Default to 30 days if completionDays is not specified
            return assignmentDate.plusDays(30);
        }

        int durationValue = completionDays.getDurationValue();

        return switch (completionDays.getDurationUnit()) {
            case DAYS -> assignmentDate.plusDays(durationValue);
            case WEEKS -> assignmentDate.plusWeeks(durationValue);
            case MONTHS -> assignmentDate.plusMonths(durationValue);
        };
    }

    @Override
    @Transactional
    public MigrateTrialUserResponseDto migrateTrialUser(MigrateTrialUserRequestDto requestDto) {
        log.info("Starting trial user migration for new clientAdminId: {}, email: {}", 
                requestDto.getClientAdminId(), requestDto.getEmail());

        // 1. Extract domain from email if not provided
        String domain = requestDto.getDomain();
        if (domain == null || domain.isBlank()) {
            String email = requestDto.getEmail();
            int atIndex = email.indexOf('@');
            if (atIndex == -1 || atIndex == email.length() - 1) {
                throw new IllegalArgumentException("Invalid email format: " + email);
            }
            domain = email.substring(atIndex + 1);
            log.info("Extracted domain from email: {}", domain);
        }

        // 2. Find trial ClientAdmin by domain where onboardBy = 'TRIAL'
        String domainPattern = "@" + domain.replace(".", "\\.");
        List<ClientAdmin> clientAdminsWithDomain = clientAdminRepository.findByEmailDomainRegex(domainPattern);
        
        ClientAdmin trialClientAdmin = clientAdminsWithDomain.stream()
                .filter(ca -> ca.getOnboardBy() == OnboardBy.TRIAL)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No trial client admin found with domain: "));

        String oldClientAdminId = trialClientAdmin.getId();
        log.info("Found trial client admin with ID: {}", oldClientAdminId);

        // 3. Validate new ClientAdmin exists
        ClientAdmin newClientAdmin = clientAdminRepository.findById(requestDto.getClientAdminId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "New client admin not found with ID: " + requestDto.getClientAdminId()));

        // 4. Find and deactivate trial subpackages
        // TODO will do it later
        // 5. Deactivate trial ClientAdmin
        trialClientAdmin.setStatus(AdminStatus.INACTIVE);
        trialClientAdmin.setEmail("deactivated_" + trialClientAdmin.getEmail());
        clientAdminRepository.save(trialClientAdmin);
        log.info("Deactivated trial client admin with ID: {}", oldClientAdminId);

        // 6. Migrate users: Find all AspireUsers with old clientAdminId and update them
        List<AspireUser> usersToMigrate = aspireUserRepository.findByUserTypeAndClientAdminId(
                UserType.USER.name(), oldClientAdminId);
        
        int migratedUsersCount = 0;
        for (AspireUser user : usersToMigrate) {
            user.setClientAdminId(requestDto.getClientAdminId());
            user.setMigratedFromClientAdminId(oldClientAdminId);
            aspireUserRepository.save(user);
            migratedUsersCount++;
        }
        log.info("Migrated {} users from old clientAdminId: {} to new clientAdminId: {}", 
                migratedUsersCount, oldClientAdminId, requestDto.getClientAdminId());

        // 7. Update new ClientAdmin domain if provided
        if (requestDto.getDomain() != null && !requestDto.getDomain().isBlank()) {
            newClientAdmin.setDomain(requestDto.getDomain());
            clientAdminRepository.save(newClientAdmin);
            log.info("Updated domain in new client admin: {}", requestDto.getDomain());
        }

        return MigrateTrialUserResponseDto.builder()
                .oldClientAdminId(oldClientAdminId)
                .newClientAdminId(requestDto.getClientAdminId())
                .migratedUsersCount(migratedUsersCount)
                .message("Trial user account migrated successfully")
                .build();
    }

    @Override
    @Transactional
    public EndUserResponseDTO updateUserStatus(String userId, UserSuspendRequestDto requestDto) {
        log.info("Updating user status for userId: {} to status: {}", userId, requestDto.getStatus());
        CurrentUserContext context = currentContextService.getCurrentUserContext();
        String currentUserId = context.getUserId();
        // Validate status
        String status = requestDto.getStatus();
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        String statusUpper = status.toUpperCase();
        if (!"SUSPEND".equals(statusUpper) && !"ACTIVE".equals(statusUpper)) {
            throw new IllegalArgumentException("Status must be either 'SUSPEND' or 'ACTIVE'");
        }

        // Get the existing user
        UUID userUuid;
        try {
            userUuid = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid userId format: " + userId);
        }

        Optional<AspireUser> existingUserOpt = aspireUserRepository.findByUserId(userUuid);
        if (existingUserOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }

        AspireUser existingUser = existingUserOpt.get();

        // Save suspend reason if provided
        if (requestDto.getSuspendReason() != null && !requestDto.getSuspendReason().isBlank()) {
            try {
                // Save suspend reason to UserSuspendReason table only when suspending
                if ("SUSPEND".equals(statusUpper)) {
                    UserSuspendReason userSuspendReason = UserSuspendReason.builder()
                                    .id(UUID.randomUUID().toString())
                                    .userId(userId)
                                    .reason(requestDto.getSuspendReason())
                                    .createdAt(Instant.now())
                                    .createdBy(currentUserId)
                                    .build();
                    
                    userSuspendReasonRepository.save(userSuspendReason);
                    log.info("Saved suspend reason to UserSuspendReason table for userId: {}, reasonId: {}", 
                            userId, requestDto.getSuspendReason());
                }
            } catch (Exception e) {
                log.warn("Failed to fetch or save suspend reason with ID: {}. Error: {}", 
                        requestDto.getSuspendReason(), e.getMessage());
                // Continue without suspend reason details
            }
        }

        // Update user status
        existingUser.setStatus(statusUpper);
        existingUser.setUpdatedBy(currentUserId);
        existingUser.setUpdatedAt(Instant.now());
        existingUser = aspireUserRepository.save(existingUser);
        log.info("Successfully updated user status to: {} for userId: {}", statusUpper, userId);

        userSessionInvalidationHelper.logoutUsersIfRestrictive(statusUpper, List.of(userId));

        // Send notification
        try {
            String userFullName = (existingUser.getFirstName() != null ? existingUser.getFirstName() : "") +
                    (existingUser.getLastName() != null ? " " + existingUser.getLastName() : "").trim();
            if (userFullName.isBlank()) {
                userFullName = existingUser.getEmail();
            }

            notificationClient.sendUserStatusChangeNotification(
                    existingUser.getEmail(),
                    statusUpper,
                    requestDto.getSuspendReason(),
                    userFullName,
                    existingUser.getCompanyName()
            );
            log.info("Status change notification sent to user: {}", existingUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to send status change notification to user: {}. Error: {}", 
                    existingUser.getEmail(), e.getMessage(), e);
            // Don't fail the operation if notification fails
        }

        // Convert to response DTO
        return mapToEndUserResponseDTO(existingUser);
    }

    @Override
    public List<EndUserResponseDTO> getAllUsers(String clientAdminId, String status, int offset, int pageSize) {
        log.info("Getting all users with filters - clientAdminId: {}, status: {}, offset: {}, pageSize: {}",
                clientAdminId, status, offset, pageSize);

        try {
            // Build query with filters
            Query query = new Query();

            // Add clientAdminId filter if provided
            if (clientAdminId != null && !clientAdminId.isBlank()) {
                query.addCriteria(Criteria.where("clientAdminId").is(clientAdminId));
            }

            // Add status filter if provided
            if (status != null && !status.isBlank()) {
                query.addCriteria(Criteria.where("status").is(status.toUpperCase()));
            }

            // Get total count before pagination
            long total = mongoTemplate.count(query, AspireUser.class);
            log.info("Total users found: {}", total);

            // Apply sorting by createdAt descending (newest first)
            query.with(Sort.by(Sort.Direction.DESC, "createdAt"));

            // Validate and apply pagination
            if (offset < 0) {
                log.warn("Invalid offset: {}. Setting to 0.", offset);
                offset = 0;
            }
            if (pageSize <= 0) {
                log.warn("Invalid pageSize: {}. Setting to 10.", pageSize);
                pageSize = 10;
            }

            // Convert offset to skip count
            int skip = offset == 0 ? 0 : (offset * pageSize);
            query.skip(skip);
            query.limit(pageSize);

            log.info("Executing query with skip: {} (offset {} * pageSize {}), limit: {}", skip, offset, pageSize, pageSize);

            // Execute query
            List<AspireUser> users = mongoTemplate.find(query, AspireUser.class);
            log.info("Found {} users out of {} total", users.size(), total);

            // Convert to response DTOs
            return users.stream()
                    .map(this::mapToEndUserResponseDTO)
                    .toList();

        } catch (Exception e) {
            log.error("Error getting all users with filters", e);
            throw new RuntimeException("Failed to retrieve users: " + e.getMessage(), e);
        }
    }

    @Override
    public long countAllUsers(String clientAdminId, String status) {
        log.info("Counting all users with filters - clientAdminId: {}, status: {}", clientAdminId, status);

        try {
            // Build query with filters
            Query query = new Query();

            // Add clientAdminId filter if provided
            if (clientAdminId != null && !clientAdminId.isBlank()) {
                query.addCriteria(Criteria.where("clientAdminId").is(clientAdminId));
            }

            // Add status filter if provided
            if (status != null && !status.isBlank()) {
                query.addCriteria(Criteria.where("status").is(status.toUpperCase()));
            }

            // Get count
            long count = mongoTemplate.count(query, AspireUser.class);
            log.info("Total count: {}", count);
            return count;

        } catch (Exception e) {
            log.error("Error counting all users with filters", e);
            throw new RuntimeException("Failed to count users: " + e.getMessage(), e);
        }
    }

    @Override
    public List<EndUserResponseDTO> getAllAspireUsersWithFilters(
            String search,
            String userType,
            String country,
            String mspId,
            String clientAdminId,
            String status,
            int offset,
            int pageSize) {
        log.info("Getting all AspireUsers with filters - search: {}, userType: {}, country: {}, mspId: {}, clientAdminId: {}, status: {}, offset: {}, pageSize: {}",
                search, userType, country, mspId, clientAdminId, status, offset, pageSize);

        try {
            // Use custom repository to fetch users
            List<AspireUser> users = endUserRepositoryCustom.findAllAspireUsersWithFilters(
                    search, userType, country, mspId, clientAdminId, status, offset, pageSize);

            log.info("Found {} AspireUsers", users.size());

            // Convert to response DTOs
            return users.stream()
                    .map(this::mapToEndUserResponseDTO)
                    .toList();

        } catch (Exception e) {
            log.error("Error getting all AspireUsers with filters", e);
            throw new RuntimeException("Failed to retrieve AspireUsers: " + e.getMessage(), e);
        }
    }

    @Override
    public long countAllAspireUsersWithFilters(
            String search,
            String userType,
            String country,
            String mspId,
            String clientAdminId,
            String status) {
        log.info("Counting all AspireUsers with filters - search: {}, userType: {}, country: {}, mspId: {}, clientAdminId: {}, status: {}",
                search, userType, country, mspId, clientAdminId, status);

        try {
            // Use custom repository to count users
            long count = endUserRepositoryCustom.countAllAspireUsersWithFilters(
                    search, userType, country, mspId, clientAdminId, status);

            log.info("Total count: {}", count);
            return count;

        } catch (Exception e) {
            log.error("Error counting all AspireUsers with filters", e);
            throw new RuntimeException("Failed to count AspireUsers: " + e.getMessage(), e);
        }
    }

    private EndUserResponseDTO mapToEndUserResponseDTO(AspireUser aspireUser) {
        String firstName = aspireUser.getFirstName() != null ? aspireUser.getFirstName() : "";
        String lastName = aspireUser.getLastName() != null ? aspireUser.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();

        // Safely convert status string to UserStatus enum
        UserStatus status = null;
        if (aspireUser.getStatus() != null && !aspireUser.getStatus().trim().isEmpty()) {
            try {
                status = UserStatus.fromString(aspireUser.getStatus());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status value '{}' for user {}, defaulting to null",
                        aspireUser.getStatus(), aspireUser.getEmail());
            }
        }

        EndUserResponseDTO.EndUserResponseDTOBuilder builder = EndUserResponseDTO.builder()
                .id(aspireUser.getUserId().toString())
                .firstName(firstName)
                .lastName(lastName)
                .fullName(fullName.isEmpty() ? null : fullName)
                .email(aspireUser.getEmail())
                .phoneNumber(aspireUser.getPhoneNumber())
                .department(aspireUser.getDepartment())
                .countryName(aspireUser.getCountry())
                .countryCode(aspireUser.getCountryCode())
                .status(status)
                .clientAdminId(aspireUser.getClientAdminId())
                .riskGroup(aspireUser.getRiskGroup())
                .lastLoginAt(aspireUser.getLastLoginAt())
                .profilePicture(aspireUser.getProfilePicture())
                .isRiskProfileExist(aspireUser.getIsRiskProfileExist());
        return builder.build();
    }

    @Override
    public PurchaseStatusResponseDTO getPurchaseStatus(String email) {
        log.info("Checking purchase status for email: {}", email);

        String trimmedEmail = email.trim().toLowerCase();

        // Check ClientAdmin table for trail or buy now (case-insensitive)
        boolean trailExists = false;
        boolean buyNowExists = false;

        // Use case-insensitive query for ClientAdmin
        Query clientAdminQuery = new Query(Criteria.where("email").regex("^" + trimmedEmail + "$", "i"));
        List<ClientAdmin> clientAdmins = mongoTemplate.find(clientAdminQuery, ClientAdmin.class);

        if (!clientAdmins.isEmpty()) {
            // Check all matching ClientAdmins for TRIAL or BUY_NOW
            for (ClientAdmin clientAdmin : clientAdmins) {
                OnboardBy onboardBy = clientAdmin.getOnboardBy();
                if (onboardBy == OnboardBy.TRIAL) {
                    trailExists = true;
                } else if (onboardBy == OnboardBy.BUY_NOW) {
                    buyNowExists = true;
                }
            }
        }

        // Check aspire_user table for user existence (case-insensitive)
        boolean alreadyRegistered = aspireUserRepository.findByEmailIgnoreCase(trimmedEmail).isPresent();

        log.info("Purchase status for email {} - trailExists: {}, buyNowExists: {}, alreadyRegistered: {}",
                email, trailExists, buyNowExists, alreadyRegistered);

        return PurchaseStatusResponseDTO.builder()
                .trailExists(trailExists)
                .buyNowExists(buyNowExists)
                .alreadyRegistered(alreadyRegistered)
                .build();
    }

    @Override
    @Transactional
    public void updateRiskProfileExist(UpdateRiskProfileExistRequestDto request) {
        if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
            log.warn("updateRiskProfileExist called with empty userIds");
            return;
        }
        List<UUID> uuids = new ArrayList<>();
        for (String idStr : request.getUserIds()) {
            try {
                uuids.add(UUID.fromString(idStr.trim()));
            } catch (IllegalArgumentException e) {
                log.warn("Skipping invalid userId: {}", idStr);
            }
        }
        if (uuids.isEmpty()) {
            log.warn("No valid userIds to update for risk profile exist");
            return;
        }
        List<AspireUser> users = aspireUserRepository.findByUserIdIn(uuids);
        if (users.isEmpty()) {
            log.info("No users found for given userIds: {}", request.getUserIds());
            return;
        }
        Boolean flag = request.getIsRiskProfileExist() != null ? request.getIsRiskProfileExist() : true;
        users.forEach(user -> user.setIsRiskProfileExist(flag));
        aspireUserRepository.saveAll(users);
        log.info("Updated isRiskProfileExist={} for {} users", flag, users.size());
    }

    @Override
    @Transactional
    public EndUserResponseDTO updateUserRiskGroup(String userId, UpdateUserRiskGroupRequestDto request) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request == null || request.getRiskGroup() == null) {
            throw new IllegalArgumentException("Risk group is required");
        }
        AspireUserDto updatedUser = aspireUserService.updateRiskGroup(userId.trim(), request.getRiskGroup());
        return convertToEndUserResponseDTO(updatedUser);
    }

    @Override
    public List<AspireUserBasicDto> getUsersByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        List<UUID> uuids = new ArrayList<>();
        for (String idStr : userIds) {
            if (idStr == null || idStr.isBlank()) {
                continue;
            }
            try {
                uuids.add(UUID.fromString(idStr.trim()));
            } catch (IllegalArgumentException e) {
                log.warn("Skipping invalid userId: {}", idStr);
            }
        }
        if (uuids.isEmpty()) {
            return List.of();
        }

        // Prefer userId field; also include document id matches for older records.
        Map<String, AspireUser> byId = new LinkedHashMap<>();
        for (AspireUser user : aspireUserRepository.findByUserIdIn(uuids)) {
            String key = user.getUserId() != null ? user.getUserId().toString() : user.getId().toString();
            byId.put(key, user);
        }
        for (AspireUser user : aspireUserRepository.findAllById(uuids)) {
            String key = user.getUserId() != null ? user.getUserId().toString() : user.getId().toString();
            byId.putIfAbsent(key, user);
            if (user.getId() != null) {
                byId.putIfAbsent(user.getId().toString(), user);
            }
        }

        return byId.values().stream()
                .map(user -> AspireUserBasicDto.builder()
                        .userId(user.getUserId() != null ? user.getUserId().toString() : user.getId().toString())
                        .email(user.getEmail())
                        .fullName(buildFullName(user.getFirstName(), user.getLastName()))
                        .department(user.getDepartment())
                        .riskGroup(user.getRiskGroup() != null ? user.getRiskGroup().name() : null)
                        .build())
                .toList();
    }

    @Override
    public List<String> findEndUserIds(List<String> clientAdminIds, String search, List<String> departments) {
        List<String> scopedIds = clientAdminIds == null ? List.of() : clientAdminIds.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        if (scopedIds.isEmpty()) {
            throw new RegistationValidationException("clientAdminId is required");
        }
        return aspireUserRepository.findEndUserIds(scopedIds, search, expandDepartmentFilters(departments));
    }

    /**
     * Users store department names. If the filter is a department document id, also match that department's name.
     */
    private List<String> expandDepartmentFilters(List<String> departments) {
        if (departments == null || departments.isEmpty()) {
            return departments;
        }
        List<String> expanded = new ArrayList<>();
        for (String dept : departments) {
            if (!StringUtils.hasText(dept)) {
                continue;
            }
            String trimmed = dept.trim();
            if (!expanded.contains(trimmed)) {
                expanded.add(trimmed);
            }
            try {
                departmentService.getDepartmentById(trimmed).ifPresent(dto -> {
                    if (dto != null && StringUtils.hasText(dto.getName()) && !expanded.contains(dto.getName().trim())) {
                        expanded.add(dto.getName().trim());
                    }
                });
            } catch (Exception e) {
                log.debug("Could not resolve department id '{}': {}", trimmed, e.getMessage());
            }
        }
        return expanded;
    }

    private String buildFullName(String firstName, String lastName) {
        String first = firstName != null ? firstName.trim() : "";
        String last = lastName != null ? lastName.trim() : "";
        return (first + " " + last).trim();
    }
}

