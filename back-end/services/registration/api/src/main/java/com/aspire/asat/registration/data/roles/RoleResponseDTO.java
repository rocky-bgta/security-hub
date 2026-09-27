package com.aspire.asat.registration.data.roles;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponseDTO {
    private String id;
    private String roleName;
    private String description;
    private Integer accessLevel;
    private String colorTheme;
    private String status;
    private boolean systemRole; // Flag to identify system roles
    private Instant createdAt;
    private Instant updatedAt;
}
