package com.aspire.asat.registration.model;

import com.aspire.asat.registration.data.roles.MenuPermissionDto;
import com.aspire.asat.registration.data.roles.RolePermissionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "role_permissions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {
    @Id
    private String id;
    private String roleId; // Reference to the Role
    private String roleName; // Name of the role
    private List<MenuPermissionDto> menuPermissions; // List of menu permissions associated with the role
    private Instant createdAt; // Timestamp for creation
    private Instant updatedAt; // Timestamp for last update

    public static RolePermission toRolePermission(RolePermissionDto rolePermissionDto) {
        return RolePermission.builder()
                .roleId(rolePermissionDto.getRoleId())
                .roleName(rolePermissionDto.getRoleName())
                .menuPermissions(rolePermissionDto.getMenuPermissions())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static RolePermissionDto toRolePermissionDto(RolePermission rolePermission) {
        return RolePermissionDto.builder()
                .roleId(rolePermission.getRoleId())
                .roleName(rolePermission.getRoleName())
                .menuPermissions(rolePermission.getMenuPermissions())
                .build();
    }
}

