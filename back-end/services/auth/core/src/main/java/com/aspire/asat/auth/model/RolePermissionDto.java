package com.aspire.asat.auth.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionDto {

    @NotNull( message = "Role ID cannot be null")
    private String roleId; // Reference to the Role
    private String roleName; // Name of the role
    private List<MenuPermissionDto> menuPermissions;
}
