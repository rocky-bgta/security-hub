package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.systemUser.request.SystemUserRequestDTO;
import com.aspire.asat.registration.data.systemUser.request.SystemUserUpdateRequestDTO;
import com.aspire.asat.registration.data.systemUser.response.SystemUserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.SYSTEM_USER_API, produces = "application/json")
@Tag(name = "System User Management", description = "Manage system users (ASPIRE_ADMIN)")
public interface SystemUserController {

    @Operation(
            summary = "Create a new system user",
            description = "Creates a new system user with ASPIRE_ADMIN role. A temporary password will be generated and sent via email."
    )
    @PostMapping
    ResponseEntity<ApiResponseDto<SystemUserResponseDTO>> createSystemUser(
            @Parameter(description = "Request body containing system user details", required = true)
            @Valid @RequestBody SystemUserRequestDTO requestDTO
    );

    @Operation(
            summary = "Get system user by ID",
            description = "Retrieves a specific system user by their unique identifier."
    )
    @GetMapping("/{userId}")
    ResponseEntity<ApiResponseDto<SystemUserResponseDTO>> getSystemUserById(
            @Parameter(description = "Unique identifier of the system user", required = true)
            @PathVariable String userId
    );

    @Operation(
            summary = "List system users",
            description = "Retrieves a paginated list of system users. Supports filtering by status and departments, and searching by name or email."
    )
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<SystemUserResponseDTO>>>> listSystemUsers(
            @Parameter(description = "Search term to filter by name or email")
            @RequestParam(required = false) String search,
            @Parameter(description = "User status to filter by")
            @RequestParam(required = false) UserStatus status,
            @Parameter(description = "List of departments to filter by (supports multiple departments)")
            @RequestParam(required = false) List<String> departments,
            @Parameter(description = "Page offset for pagination", example = "0")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Page size for pagination", example = "10")
            @RequestParam(defaultValue = "10") int pageSize
    );
    @Operation(
            summary = "Update system user",
            description = "Updates the details and status of an existing system user. Can update user details and/or change status in a single request."
    )
    @PutMapping("/{userId}")
    ResponseEntity<ApiResponseDto<SystemUserResponseDTO>> updateSystemUser(
            @Parameter(description = "Unique identifier of the system user", required = true)
            @PathVariable String userId,
            @Parameter(description = "Updated system user details and status", required = true)
            @Valid @RequestBody SystemUserUpdateRequestDTO requestDTO
    );

}
