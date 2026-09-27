package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.AttackTechniqueCreateRequest;
import com.aspire.asat.phishing.dto.request.AttackTechniqueUpdateRequest;
import com.aspire.asat.phishing.dto.response.AttackTechniqueDto;
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
 * Controller interface for configurable attack technique catalog.
 */
@Tag(name = "Attack techniques", description = "APIs for managing configurable attack techniques")
@RequestMapping(value = WebApiUrlConstants.ATTACK_TECHNIQUES_PATH, produces = "application/json")
public interface AttackTechniqueController {

    @Operation(summary = "Get list of attack techniques",
            description = "Retrieve paginated list with optional name search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attack techniques retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<AttackTechniqueDto>>>> getAttackTechniques(
            @Parameter(description = "Optional search term (matches name, case-insensitive substring)")
            @RequestParam(required = false) String searchParam,
            @Parameter(description = "Filter by effective active entries (includes missing isActive in DB)")
            @RequestParam(defaultValue = "true") boolean isActive,
            @Parameter(description = "Page index (0-based), same semantics as email-templates list")
            @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "Field to sort by (displayOrder, name, createdAt)")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortOrder
    );

    @Operation(summary = "Get attack technique by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attack technique retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Attack technique not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<AttackTechniqueDto>> getAttackTechniqueById(
            @Parameter(description = "Attack technique ID") @PathVariable String id
    );

    @Operation(summary = "Create attack technique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attack technique created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<AttackTechniqueDto>> createAttackTechnique(
            @Valid @RequestBody AttackTechniqueCreateRequest request
    );

    @Operation(summary = "Update attack technique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attack technique updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Attack technique not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<AttackTechniqueDto>> updateAttackTechnique(
            @Parameter(description = "Attack technique ID") @PathVariable String id,
            @Valid @RequestBody AttackTechniqueUpdateRequest request
    );

    @Operation(summary = "Delete attack technique")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attack technique deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Attack technique not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteAttackTechnique(
            @Parameter(description = "Attack technique ID") @PathVariable String id
    );
}
