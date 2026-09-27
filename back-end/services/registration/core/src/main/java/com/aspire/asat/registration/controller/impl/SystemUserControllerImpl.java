package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.systemUser.request.SystemUserRequestDTO;
import com.aspire.asat.registration.data.systemUser.request.SystemUserUpdateRequestDTO;
import com.aspire.asat.registration.data.systemUser.response.SystemUserResponseDTO;
import com.aspire.asat.registration.controller.SystemUserController;
import com.aspire.asat.registration.service.SystemUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SystemUserControllerImpl implements SystemUserController {

    private final SystemUserService systemUserService;

    public SystemUserControllerImpl(SystemUserService systemUserService) {
        this.systemUserService = systemUserService;
    }

    @Override
    public ResponseEntity<ApiResponseDto<SystemUserResponseDTO>> createSystemUser(SystemUserRequestDTO requestDTO) {
        SystemUserResponseDTO systemUser = systemUserService.createSystemUser(requestDTO);
        ApiResponseDto<SystemUserResponseDTO> response = ApiResponseDto.<SystemUserResponseDTO>builder()
                .data(systemUser)
                .message("System user created successfully")
                .statusCode(201)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<SystemUserResponseDTO>> getSystemUserById(String userId) {
        SystemUserResponseDTO systemUserOpt = systemUserService.getSystemUserById(userId);
        ApiResponseDto<SystemUserResponseDTO> response = ApiResponseDto.<SystemUserResponseDTO>builder()
                .data(systemUserOpt)
                .message("System user retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<SystemUserResponseDTO>>>> listSystemUsers(
            String search, UserStatus status, List<String> departments, int offset, int pageSize) {
        
        List<SystemUserResponseDTO> users = systemUserService.listSystemUsers(search, status, departments, offset, pageSize);
        long total = systemUserService.countSystemUsers(search, status, departments);

        AllResponseDto<List<SystemUserResponseDTO>> response = new AllResponseDto<>(offset, pageSize, total, users);
        return ResponseEntity.ok(new ApiResponseDto<>("System users fetched", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<SystemUserResponseDTO>> updateSystemUser(String userId, SystemUserUpdateRequestDTO requestDTO) {
        SystemUserResponseDTO systemUser = systemUserService.updateSystemUser(userId, requestDTO);
        ApiResponseDto<SystemUserResponseDTO> response = ApiResponseDto.<SystemUserResponseDTO>builder()
                .data(systemUser)
                .message("System user updated successfully")
                .statusCode(HttpStatus.OK.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
