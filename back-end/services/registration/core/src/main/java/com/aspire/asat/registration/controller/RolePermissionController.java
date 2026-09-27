package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.registration.data.roles.MenuPermissionResponseDTO;
import com.aspire.asat.registration.data.roles.RolePermissionDto;
import com.aspire.asat.registration.data.roles.UserMenuPermissionResponse;
import com.aspire.asat.registration.constant.WebApiUrlConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag( name = "Role Permission Management", description = "APIs for managing role permissions")
@RequestMapping(value = WebApiUrlConstants.ROLE_PERMISSION_API)
public interface RolePermissionController {
    @PostMapping
    ResponseEntity<ApiResponse<RolePermissionDto>> saveRolePermission(@Valid @RequestBody RolePermissionDto rolePermissionDto, HttpServletRequest httpRequest);

    @GetMapping(value = "/menu")
    ResponseEntity<ApiResponse<List<MenuPermissionResponseDTO>>> getMenuPermissionsByRoleId(
            @RequestParam("role") String roleId,
            @RequestParam(value = "search", required = false) String search,
            HttpServletRequest httpRequest);

    @GetMapping("/{username}")
    @Operation(summary = "Get current user's role permissions", description = "Fetches the permissions associated with the current user's role.")
    ResponseEntity<ApiResponse<List<UserMenuPermissionResponse>>> getCurrentUserRolePermissions(@PathVariable String username, HttpServletRequest httpRequest);
}
