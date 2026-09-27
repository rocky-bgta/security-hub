package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.VishingAttackTemplateCreateRequest;
import com.aspire.asat.phishing.dto.request.VishingAttackTemplateUpdateRequest;
import com.aspire.asat.phishing.dto.response.VishingAttackTemplateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller for platform-managed vishing attack template catalog.
 * Create/update/delete: ASPIRE_ADMIN / SUPER_ADMIN / SYSTEM_USER only.
 * List/get: all authenticated users (for scenario Attack Template picker).
 */
@Tag(name = "Vishing Attack Templates",
        description = "Platform attack template catalog used by vishing scenarios")
@RequestMapping(value = WebApiUrlConstants.VISHING_ATTACK_TEMPLATES_PATH, produces = "application/json")
public interface VishingAttackTemplateController {

    @Operation(summary = "List vishing attack templates",
            description = "Paginated list with optional name search. Available to all authenticated users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attack templates retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<VishingAttackTemplateDto>>>> list(
            @Parameter(description = "Optional search term (matches name, case-insensitive substring)")
            @RequestParam(required = false) String searchParam,
            @Parameter(description = "Page index (0-based)")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Field to sort by (name, createdAt)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortOrder
    );

    @Operation(summary = "Get vishing attack template by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attack template retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Attack template not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<VishingAttackTemplateDto>> getById(
            @Parameter(description = "Attack template ID") @PathVariable String id
    );

    @Operation(summary = "Create vishing attack template",
            description = "Platform admin only (ASPIRE_ADMIN / SUPER_ADMIN / SYSTEM_USER). Variables are auto-detected from script.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attack template created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or permission denied")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<VishingAttackTemplateDto>> create(
            @Valid @RequestBody VishingAttackTemplateCreateRequest request
    );

    @Operation(summary = "Update vishing attack template",
            description = "Platform admin only. Variables are re-detected from script.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attack template updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or permission denied"),
            @ApiResponse(responseCode = "404", description = "Attack template not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<VishingAttackTemplateDto>> update(
            @Parameter(description = "Attack template ID") @PathVariable String id,
            @Valid @RequestBody VishingAttackTemplateUpdateRequest request
    );

    @Operation(summary = "Delete vishing attack template", description = "Platform admin only")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attack template deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Permission denied"),
            @ApiResponse(responseCode = "404", description = "Attack template not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> delete(
            @Parameter(description = "Attack template ID") @PathVariable String id
    );
}
