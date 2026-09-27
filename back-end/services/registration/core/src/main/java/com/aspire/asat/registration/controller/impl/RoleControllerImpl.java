package com.aspire.asat.registration.controller.impl;


import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.roles.RoleRequestDTO;
import com.aspire.asat.registration.data.roles.RoleResponseDTO;
import com.aspire.asat.registration.controller.RoleController;
import com.aspire.asat.registration.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RoleControllerImpl implements RoleController {

    private final RoleService roleService;

    @Override
    public ResponseEntity<ApiResponseDto<RoleResponseDTO>> createRole(RoleRequestDTO roleRequestDTO) {

        RoleResponseDTO createdRole = roleService.createRole(roleRequestDTO);
        ApiResponseDto<RoleResponseDTO> response = new ApiResponseDto<>("Role created successfully", 201, createdRole);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<RoleResponseDTO>>> getAllRoles(String search, String sortBy, String order) {
        List<RoleResponseDTO> roles = roleService.getAllRoles(search, sortBy, order);
        ApiResponseDto<List<RoleResponseDTO>> response = new ApiResponseDto<>("Roles fetched successfully", 200, roles);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

//    @Override
//    public ResponseEntity<ApiResponseDto<List<RoleResponseDTO>>> getAllRoles() {
//        List<RoleResponseDTO> roles = roleService.getAllRoles();
//        ApiResponseDto<List<RoleResponseDTO>> response = new ApiResponseDto<>("Roles fetched successfully", 200, roles);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }

    @Override
    public ResponseEntity<ApiResponseDto<RoleResponseDTO>> getRoleById(String id) {
        RoleResponseDTO role = roleService.getRoleById(id);
        ApiResponseDto<RoleResponseDTO> response = new ApiResponseDto<>("Role fetched successfully", 200, role);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<RoleResponseDTO>> updateRoleById(String id, RoleRequestDTO roleRequestDTO) {
        RoleResponseDTO updatedRole = roleService.updateRoleById(id, roleRequestDTO);
        ApiResponseDto<RoleResponseDTO> response = new ApiResponseDto<>("Role updated successfully", 200, updatedRole);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<RoleResponseDTO>> deleteRoleById(String id) {
        RoleResponseDTO deletedRole = roleService.deleteRoleById(id);
        ApiResponseDto<RoleResponseDTO> response = new ApiResponseDto<>("Role deleted successfully", 200, deletedRole);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
