package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.data.enums.SystemRole;
import com.aspire.asat.registration.model.Role;
import com.aspire.asat.registration.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service responsible for initializing system roles at application startup.
 * Ensures all predefined system roles exist in the database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemRoleInitializationService implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting system role initialization...");
        initializeSystemRoles();
        log.info("System role initialization completed.");
    }

    /**
     * Initialize all system roles defined in the SystemRole enum.
     * Creates roles that don't exist, skips existing ones.
     */
    private void initializeSystemRoles() {
        for (SystemRole systemRole : SystemRole.values()) {
            try {
                initializeSystemRole(systemRole);
            } catch (Exception e) {
                log.error("Failed to initialize system role: {}", systemRole.getRoleName(), e);
                // Continue with other roles even if one fails
            }
        }
    }

    /**
     * Initialize a specific system role.
     * @param systemRole the system role enum to initialize
     */
    private void initializeSystemRole(SystemRole systemRole) {
        String roleName = systemRole.getRoleName();
        
        // Check if role already exists
        if (roleRepository.existsByRoleName(roleName)) {
            log.debug("System role '{}' already exists, skipping initialization.", roleName);
            return;
        }

        // Create the system role
        Role role = Role.builder()
                .roleName(roleName)
                .description(systemRole.getDescription())
                .accessLevel(systemRole.getAccessLevel())
                .colorTheme(systemRole.getColorTheme())
                .status("ACTIVE")
                .systemRole(true) // Mark as system role
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Role savedRole = roleRepository.save(role);
        log.info("Initialized system role: {} with ID: {}", savedRole.getRoleName(), savedRole.getId());
    }

    /**
     * Get count of system roles in the database.
     * @return number of system roles
     */
    public long getSystemRoleCount() {
        return roleRepository.findAll().stream()
                .filter(Role::isSystemRole)
                .count();
    }

    /**
     * Check if all system roles are properly initialized.
     * @return true if all system roles exist
     */
    public boolean areAllSystemRolesInitialized() {
        for (SystemRole systemRole : SystemRole.values()) {
            if (!roleRepository.existsByRoleName(systemRole.getRoleName())) {
                return false;
            }
        }
        return true;
    }
}
