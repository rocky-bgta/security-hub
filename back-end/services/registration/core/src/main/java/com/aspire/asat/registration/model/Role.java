package com.aspire.asat.registration.model;

import com.aspire.asat.registration.data.roles.RoleRequestDTO;
import com.aspire.asat.registration.data.roles.RoleResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    private String id; // MongoDB uses String IDs by default
    private String roleName;
    private String description;
    private Integer accessLevel; // 1 for basic, 2 for advanced, etc.
    private String colorTheme; // e.g., "#FF5733"
    private String status; // "ACTIVE" or "INACTIVE"
    @Builder.Default
    private boolean systemRole = false; // Flag to identify system roles
    private Instant createdAt; // Timestamp for creation
    private Instant updatedAt; // Timestamp for last update

    public static Role toRole(RoleRequestDTO roleRequestDTO) {
        return Role.builder()
                .roleName(roleRequestDTO.getRoleName())
                .description(roleRequestDTO.getDescription())
                .accessLevel(roleRequestDTO.getAccessLevel())
                .colorTheme(roleRequestDTO.getColorTheme())
                .status(roleRequestDTO.getStatus())
                .systemRole(false) // User-created roles are never system roles
                .createdAt(Instant.now()) // Set current time for creation
                .updatedAt(Instant.now()) // Set current time for last update
                .build();
    }

    public static RoleResponseDTO toRoleResponseDTO(Role role) {
        return RoleResponseDTO.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .description(role.getDescription())
                .accessLevel(role.getAccessLevel())
                .colorTheme(role.getColorTheme())
                .status(role.getStatus())
                .systemRole(role.isSystemRole())
                .createdAt(role.getCreatedAt()) // Assuming createdAt is a field in Role
                .updatedAt(role.getUpdatedAt()) // Assuming updatedAt is a field in Role
                .build();
    }
}
