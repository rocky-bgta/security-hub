package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.roles.MenuPermissionResponseDTO;
import com.aspire.asat.registration.data.roles.RolePermissionDto;
import com.aspire.asat.registration.data.roles.UserMenuPermissionResponse;
import com.aspire.asat.common.dto.files.CurrentUserContext;

import java.util.List;

public interface RolePermissionService {
    RolePermissionDto saveRolePermission(RolePermissionDto rolePermissionDto);

    List<MenuPermissionResponseDTO> getMenuPermissionsByRoleId(String roleId, String search);

    List<UserMenuPermissionResponse> getCurrentUserRolePermissions(CurrentUserContext context, String username);
}
