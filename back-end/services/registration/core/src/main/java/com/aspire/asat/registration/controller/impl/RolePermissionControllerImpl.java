package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.registration.controller.RolePermissionController;
import com.aspire.asat.registration.controller.base.BaseController;
import com.aspire.asat.registration.data.ApiResponse;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.registration.data.enums.ResponseMessage;
import com.aspire.asat.registration.data.roles.MenuPermissionResponseDTO;
import com.aspire.asat.registration.data.roles.RolePermissionDto;
import com.aspire.asat.registration.data.roles.UserMenuPermissionResponse;
import com.aspire.asat.registration.service.RolePermissionService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class RolePermissionControllerImpl extends BaseController implements RolePermissionController {
    private final RolePermissionService rolePermissionService;
    private final UserCurrentContextService  userCurrentContextService;
    private final MessageService messageService;


    @Override
    public ResponseEntity<ApiResponse<RolePermissionDto>> saveRolePermission(RolePermissionDto rolePermissionDto, HttpServletRequest httpRequest) {
        log.info("Saving role permission: {}", rolePermissionDto);
//        RolePermissionDto savedRolePermission = rolePermissionService.saveRolePermission(rolePermissionDto);
//        ApiResponseDto<RolePermissionDto> response = new ApiResponseDto<>("Role permission saved successfully", 201, savedRolePermission);
//        return ResponseEntity.status(201).body(response);
        return handleRequest(
                () -> rolePermissionService.saveRolePermission(rolePermissionDto),
                messageService.get(MessageKeys.ROLE_PERMISSION_UPDATED),
                httpRequest
        );
    }


    @Override
    public ResponseEntity<ApiResponse<List<MenuPermissionResponseDTO>>> getMenuPermissionsByRoleId(String roleId, String search, HttpServletRequest httpRequest) {
        log.info("Fetching menu permissions for role ID: {}, search filter: {}", roleId, search);
        return handleRequest(
                () -> rolePermissionService.getMenuPermissionsByRoleId(roleId, search),
                ResponseMessage.FETCHED_SUCCESS.getResponseMessage(),
                httpRequest
        );
    }

    @Override
    public ResponseEntity<ApiResponse<List<UserMenuPermissionResponse>>> getCurrentUserRolePermissions(String username, HttpServletRequest httpRequest) {
        log.info("Fetching current user role permissions");
        return handleRequest(
                () -> {
                    CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
                    return rolePermissionService.getCurrentUserRolePermissions(context, username);
                },
                ResponseMessage.OPERATION_SUCCESSFUL.getResponseMessage(),
                httpRequest
        );
    }
}
