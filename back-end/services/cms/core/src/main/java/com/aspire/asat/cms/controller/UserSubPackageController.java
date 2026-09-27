package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.user.UserSubPackageAssignRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "User SubPackage Management", description = "APIs for managing user subpackage assignments")
@RequestMapping("/api/v1/user-subpackages")
public interface UserSubPackageController {

    @Operation(summary = "Assign subpackages to users", description = "Creates user subpackage assignments and related progress tracking")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subpackages assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/assign")
    ResponseEntity<ApiResponseDto<Void>> assignSubPackagesToUsers(@Valid @RequestBody UserSubPackageAssignRequest request);

    @Operation(summary = "Reset user subpackage progress", description = "Completely resets user's progress for a specific sub-package including topics, content status, and exam data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User subpackage progress reset successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "User subpackage not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/reset")
    ResponseEntity<ApiResponseDto<Void>> resetUserSubPackage(
            @Parameter(description = "User ID", required = true) @RequestParam @NotBlank String userId,
            @Parameter(description = "SubPackage ID", required = true) @RequestParam @NotBlank String subPackageId
    );
}
