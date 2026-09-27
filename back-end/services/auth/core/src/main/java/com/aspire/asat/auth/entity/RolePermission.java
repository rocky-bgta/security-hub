package com.aspire.asat.auth.entity;


import com.aspire.asat.auth.model.MenuPermissionDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "role_permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {
    @Id
    private String id;
    private String roleId;
    private String roleName;
    private List<MenuPermissionDto> menuPermissions;
    private Instant createdAt;
    private Instant updatedAt;
}

