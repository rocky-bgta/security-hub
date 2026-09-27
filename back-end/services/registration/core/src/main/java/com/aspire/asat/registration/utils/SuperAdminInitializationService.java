package com.aspire.asat.registration.utils;

import com.aspire.asat.registration.data.dto.AspireUserCreateRequestDto;
import com.aspire.asat.registration.data.dto.AspireUserDto;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.model.Role;
import com.aspire.asat.registration.repository.RoleRepository;
import com.aspire.asat.registration.service.AspireUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service to initialize superadmin user on application startup
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuperAdminInitializationService {

    private final AspireUserService aspireUserService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Value("${system.user.email}")
    private String superAdminEmail;

    private static final String SUPERADMIN_PASSWORD = "123456789";
    private static final String SUPERADMIN_FIRST_NAME = "Super";
    private static final String SUPERADMIN_LAST_NAME = "Admin";

    @EventListener(ApplicationReadyEvent.class)
    public void initializeSuperAdmin() {
        try {
            log.info("Starting superadmin initialization...");

            // Check if superadmin already exists
            if (aspireUserService.getUserByEmail(superAdminEmail).isPresent()) {
                log.info("Superadmin user already exists with email: {}", superAdminEmail);
                return;
            }

            // Create superadmin user
            createSuperAdminUser();

            log.info("Superadmin initialization completed successfully");

        } catch (Exception e) {
            log.error("Failed to initialize superadmin user", e);
        }
    }

    private void createSuperAdminUser() {
        log.info("Creating superadmin user with email: {}", superAdminEmail);

        // Generate a unique base user ID
        UUID baseUserId = CommonUtils.generateUUID();

        // Encode the password
        String encodedPassword = passwordEncoder.encode(SUPERADMIN_PASSWORD);

        // Create the superadmin user request
        AspireUserCreateRequestDto superAdminRequest = AspireUserCreateRequestDto.builder()
                .baseUserId(baseUserId)
                .firstName(SUPERADMIN_FIRST_NAME)
                .lastName(SUPERADMIN_LAST_NAME)
                .email(superAdminEmail)
                .password(encodedPassword)
                .roles(convertRoleNamesToIds(List.of(UserType.SUPER_ADMIN.name())))
                .userType(UserType.SUPER_ADMIN.name())
                .status("ACTIVE")
                .createdBy("SYSTEM") // System-created user
                .plainPassword(SUPERADMIN_PASSWORD) // Store plain password for reference
                .skipPasswordValidation(true) // Bootstrap/system user; do not enforce policy
                .isDefault(true) // Force password change on first login
                .build();

        // Create the user
        AspireUserDto createdUser = aspireUserService.createUser(superAdminRequest);

        if (createdUser != null) {
            log.info("Superadmin user created successfully with ID: {}", createdUser.getBaseUserId());
        } else {
            log.error("Failed to create superadmin user");
        }
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
}
