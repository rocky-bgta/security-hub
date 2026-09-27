package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.exception.ResourceAlreadyExistsException;
import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.registration.data.dto.AspireUserCreateRequestDto;
import com.aspire.asat.registration.data.dto.AspireUserDto;
import com.aspire.asat.registration.data.enums.RiskGroup;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.data.systemUser.request.SystemUserRequestDTO;
import com.aspire.asat.registration.data.systemUser.request.SystemUserUpdateRequestDTO;
import com.aspire.asat.registration.data.systemUser.response.SystemUserResponseDTO;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.model.Role;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.RoleRepository;
import com.aspire.asat.registration.service.AspireUserService;
import com.aspire.asat.registration.service.SystemUserService;
import com.aspire.asat.registration.service.support.UserSessionInvalidationHelper;
import com.aspire.asat.registration.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemUserServiceImpl implements SystemUserService {

    private final PasswordEncoder passwordEncoder;
    private final AspireUserService aspireUserService;
    private final RegistrationNotificationClient registrationNotificationClient;
    private final RoleRepository roleRepository;
    private final AspireUserRepository aspireUserRepository;
    private final UserSessionInvalidationHelper userSessionInvalidationHelper;

    @Override
    public SystemUserResponseDTO createSystemUser(SystemUserRequestDTO requestDTO) {
        log.info("Creating system user with email: {}", requestDTO.getEmail());

        // 1. Validate email doesn't already exist
        if (aspireUserRepository.existsByEmail(requestDTO.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists: " + requestDTO.getEmail());
        }

        // 2. Validate role IDs
        roleIdValidates(requestDTO.getRoleIds());

        // 3. Generate a temporary password
        String tempPassword = CommonUtils.generateTemporaryPassword();

        // 4. Encrypt the password
        String hashedPassword = passwordEncoder.encode(tempPassword);

        // 5. Create AspireUser record with SYSTEM_USER type
        UUID systemUserId = UUID.randomUUID();
        AspireUserCreateRequestDto aspireUserRequest = AspireUserCreateRequestDto.builder()
                .baseUserId(systemUserId)
                .firstName(requestDTO.getFirstName())
                .lastName(requestDTO.getLastName())
                .email(requestDTO.getEmail())
                .password(hashedPassword)
                .roles(requestDTO.getRoleIds()) // Use the provided role IDs
                .userType(UserType.SYSTEM_USER.name()) // Use SYSTEM_USER type
                .status(UserStatus.ACTIVE.name()) // Default status
                .createdBy("SYSTEM") // System created user
                .plainPassword(tempPassword) // Store plain password for email
                .skipPasswordValidation(true) // System-generated temp password; preserve existing behavior
                .department(requestDTO.getDepartment())
                .companyName(requestDTO.getCompanyName())
                .designation(requestDTO.getDesignation()) // Map designation
                .supervisorName(requestDTO.getSupervisorName()) // Map supervisor name
                .country(requestDTO.getCountry())
                .riskGroup(RiskGroup.HIGH_RISK) // Set the default risk group
                .build();

        // 6. Create the user in the aspire_user table
        AspireUserDto aspireUser = aspireUserService.createUser(aspireUserRequest);
        log.info("Created AspireUser with ID: {} for system user: {}", aspireUser.getBaseUserId(), requestDTO.getEmail());

        // 7. Send welcome email with temporary password
        String fullName = requestDTO.getFirstName().trim()
                + (requestDTO.getLastName() != null && !requestDTO.getLastName().isBlank()
                ? " " + requestDTO.getLastName().trim() : "");
        registrationNotificationClient.sendWelcomeEmailNotification(
                requestDTO.getEmail(),
                systemUserId.toString(),
                null,
                fullName,
                tempPassword
        );

        // 8. Prepare and return the response DTO
        return SystemUserResponseDTO.builder()
                .id(aspireUser.getBaseUserId().toString())
                .firstName(requestDTO.getFirstName())
                .lastName(requestDTO.getLastName())
                .email(requestDTO.getEmail())
                .companyName(requestDTO.getCompanyName())
                .designation(requestDTO.getDesignation())
                .department(requestDTO.getDepartment())
                .country(requestDTO.getCountry())
                .zipCode(requestDTO.getZipCode())
                .supervisorName(requestDTO.getSupervisorName())
                .status(aspireUser.getStatus())
                .riskGroup(aspireUser.getRiskGroup())
                .roles(getRoleNames(requestDTO.getRoleIds()))
                .createdAt(aspireUser.getCreatedAt())
                .lastLoginAt(aspireUser.getLastLoginAt())
                .build();
    }

    private void roleIdValidates(List<String> roleIds) {
        // 2. Validate all role IDs exist
        List<String> invalidRoleIds = roleIds.stream()
                .filter(roleId -> !roleRepository.existsById(roleId))
                .toList();

        if (!invalidRoleIds.isEmpty()) {
            throw new ResourceNotFoundException("Invalid role IDs: " + String.join(", ", invalidRoleIds));
        }
    }

    private List<String> getRoleNames(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        return roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .map(Role::getRoleName)
                        .orElse("Unknown Role"))
                .toList();
    }

    @Override
    public SystemUserResponseDTO getSystemUserById(String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            Optional<AspireUserDto> aspireUserOpt = aspireUserService.getUserById(userUuid);

            if (aspireUserOpt.isEmpty()) {
                throw new ResourceNotFoundException("System user not found with ID: " + userId);
            }

            AspireUserDto aspireUser = aspireUserOpt.get();

            return SystemUserResponseDTO.builder()
                    .id(aspireUser.getBaseUserId().toString())
                    .firstName(aspireUser.getFirstName())
                    .lastName(aspireUser.getLastName())
                    .email(aspireUser.getEmail())
                    .companyName(aspireUser.getCompanyName())
                    .designation(aspireUser.getDesignation())
                    .department(aspireUser.getDepartment())
                    .country(aspireUser.getCountry())
                    .supervisorName(aspireUser.getSupervisorName())
                    .status(aspireUser.getStatus())
                    .riskGroup(aspireUser.getRiskGroup())
                    .roles(getRoleNames(aspireUser.getRoles()))
                    .createdAt(aspireUser.getCreatedAt())
                    .lastLoginAt(aspireUser.getLastLoginAt())
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving system user with ID {}: {}", userId, e.getMessage(), e);
            throw new ResourceNotFoundException("System user not found with ID: " + userId);
        }
    }

    @Override
    public List<SystemUserResponseDTO> listSystemUsers(String search, UserStatus status, List<String> departments, int offset, int pageSize) {
        log.info("Listing system users with search: {}, status: {}, departments: {}, offset: {}, pageSize: {}",
                search, status, departments, offset, pageSize);

        try {
            // Get all system users
            List<AspireUserDto> aspireUsers = aspireUserService.getUsersByType(UserType.SYSTEM_USER.name());
            log.info("Total system users retrieved from database: {}", aspireUsers.size());

            // Filter by criteria
            List<AspireUserDto> filteredUsers = getFilteredSystemUsers(search, status, departments, aspireUsers);

            log.info("Total system users after filtering: {}", filteredUsers.size());
            log.info("Applying pagination: offset={}, pageSize={}", offset, pageSize);

            // Validate pagination parameters
            if (offset < 0) {
                log.warn("Invalid offset: {}. Setting to 0.", offset);
                offset = 0;
            }
            if (pageSize <= 0) {
                log.warn("Invalid pageSize: {}. Setting to 10.", pageSize);
                pageSize = 10;
            }

            int skipSize = offset == 0 ? 0 : (offset * pageSize);

            // Apply pagination
            List<AspireUserDto> paginatedUsers = filteredUsers.stream()
                    .skip(skipSize)
                    .limit(pageSize)
                    .toList();

            log.info("System users after pagination: {}", paginatedUsers.size());

            // Convert to SystemUserResponseDTO
            return paginatedUsers.stream()
                    .map(this::convertToSystemUserResponseDTOFromDto)
                    .toList();

        } catch (Exception e) {
            log.error("Error listing system users", e);
            return new ArrayList<>();
        }
    }

    @Override
    public long countSystemUsers(String search, UserStatus status, List<String> departments) {
        log.info("Counting system users with search: {}, status: {}, departments: {}", search, status, departments);

        try {
            // Get all system users
            List<AspireUserDto> aspireUsers = aspireUserService.getUsersByType(UserType.SYSTEM_USER.name());

            // Filter by criteria
            List<AspireUserDto> filteredUsers = getFilteredSystemUsers(search, status, departments, aspireUsers);

            return filteredUsers.size();
        } catch (Exception e) {
            log.error("Error counting system users", e);
            return 0;
        }
    }

    private List<AspireUserDto> getFilteredSystemUsers(String search, UserStatus status, List<String> departments, List<AspireUserDto> aspireUsers) {
        return aspireUsers.stream()
                .filter(user -> status == null || user.getStatus().equals(status.name()))
                .filter(user -> departments == null || departments.isEmpty() ||
                        (user.getDepartment() != null && departments.stream()
                                .anyMatch(dept -> dept != null && dept.equalsIgnoreCase(user.getDepartment()))))
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
                .toList();
    }

    private SystemUserResponseDTO convertToSystemUserResponseDTOFromDto(AspireUserDto aspireUser) {
        return SystemUserResponseDTO.builder()
                .id(aspireUser.getBaseUserId().toString())
                .firstName(aspireUser.getFirstName())
                .lastName(aspireUser.getLastName())
                .email(aspireUser.getEmail())
                .companyName(aspireUser.getCompanyName())
                .designation(aspireUser.getDesignation())
                .department(aspireUser.getDepartment())
                .country(aspireUser.getCountry())
                .supervisorName(aspireUser.getSupervisorName())
                .status(aspireUser.getStatus())
                .riskGroup(aspireUser.getRiskGroup())
                .roles(getRoleNames(aspireUser.getRoles()))
                .createdAt(aspireUser.getCreatedAt())
                .lastLoginAt(aspireUser.getLastLoginAt())
                .build();
    }

    @Override
    public SystemUserResponseDTO updateSystemUser(String userId, SystemUserUpdateRequestDTO requestDTO) {
        log.info("Updating system user with ID: {}", userId);

        AspireUser systemUser = getSystemUser(userId);

        updateIfNotNull(requestDTO.getFirstName(), systemUser::setFirstName);
        updateIfNotNull(requestDTO.getLastName(), systemUser::setLastName);
        updateIfNotNull(requestDTO.getEmail(), systemUser::setEmail);
        updateIfNotNull(requestDTO.getDepartment(), systemUser::setDepartment);
        updateIfNotNull(requestDTO.getDesignation(), systemUser::setDesignation);
        updateIfNotNull(requestDTO.getSupervisorName(), systemUser::setSupervisorName);


        updateIfNotNull(requestDTO.getStatus().name(), systemUser::setStatus);


        if (requestDTO.getRoleIds() != null && !requestDTO.getRoleIds().isEmpty()) {
            roleIdValidates(requestDTO.getRoleIds());
            systemUser.setRoles(requestDTO.getRoleIds());
        }

        AspireUser updatedUser = aspireUserRepository.save(systemUser);

        if (requestDTO.getStatus() != null) {
            String statusName = requestDTO.getStatus().name();
            String logoutUserId = updatedUser.getUserId() != null
                    ? updatedUser.getUserId().toString()
                    : userId;
            userSessionInvalidationHelper.logoutUsersIfRestrictive(statusName, List.of(logoutUserId));
        }

        return convertToSystemUserResponseDTO(updatedUser);
    }

    private <T> void updateIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private AspireUser getSystemUser(String userId) {
        AspireUser aspireUser = aspireUserRepository.findByUserId(UUID.fromString(userId)).orElseThrow(
                () -> new ResourceNotFoundException("System user not found with ID: " + userId)
        );

        // Verify this is a system user
        if (!UserType.SYSTEM_USER.name().equals(aspireUser.getUserType())) {
            throw new IllegalArgumentException("User is not a system user: " + userId);
        }

        return aspireUser;
    }


    private SystemUserResponseDTO convertToSystemUserResponseDTO(AspireUser aspireUser) {
        return SystemUserResponseDTO.builder()
                .id(aspireUser.getUserId().toString())
                .firstName(aspireUser.getFirstName())
                .lastName(aspireUser.getLastName())
                .email(aspireUser.getEmail())
                .companyName(aspireUser.getCompanyName())
                .designation(aspireUser.getDesignation())
                .department(aspireUser.getDepartment())
                .country(aspireUser.getCountry())
                .supervisorName(aspireUser.getSupervisorName())
                .status(aspireUser.getStatus())
                .riskGroup(aspireUser.getRiskGroup())
                .roles(getRoleNames(aspireUser.getRoles()))
                .createdAt(aspireUser.getCreatedAt())
                .lastLoginAt(aspireUser.getLastLoginAt())
                .build();
    }

}
