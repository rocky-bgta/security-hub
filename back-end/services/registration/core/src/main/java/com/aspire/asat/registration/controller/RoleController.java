package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.roles.RoleRequestDTO;
import com.aspire.asat.registration.data.roles.RoleResponseDTO;
import com.aspire.asat.registration.constant.WebApiUrlConstants;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Role Management", description = "APIs for managing roles and permissions")
@RequestMapping(value = WebApiUrlConstants.ROLE_API, produces = "application/json")
public interface RoleController {
    @PostMapping
    ResponseEntity<ApiResponseDto<RoleResponseDTO>> createRole(@Valid @RequestBody RoleRequestDTO roleRequestDTO);

    @GetMapping
    ResponseEntity<ApiResponseDto<List<RoleResponseDTO>>> getAllRoles(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order
    );

    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<RoleResponseDTO>> getRoleById(@PathVariable String id);

    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<RoleResponseDTO>> updateRoleById(@PathVariable String id, @Valid @RequestBody RoleRequestDTO roleRequestDTO);

    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<RoleResponseDTO>> deleteRoleById(@PathVariable String id);
}
