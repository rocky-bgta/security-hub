package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.DeceptionLevelCreateRequest;
import com.aspire.asat.phishing.dto.request.DeceptionLevelUpdateRequest;
import com.aspire.asat.phishing.dto.response.DeceptionLevelDto;
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
 * Controller interface for configurable deception level catalog.
 */
@Tag(name = "Deception levels", description = "APIs for managing configurable deception levels")
@RequestMapping(value = WebApiUrlConstants.DECEPTION_LEVELS_PATH, produces = "application/json")
public interface DeceptionLevelController {

    @Operation(summary = "Get list of deception levels",
            description = "Retrieve paginated list with optional name search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deception levels retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<DeceptionLevelDto>>>> getDeceptionLevels(
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

    @Operation(summary = "Get deception level by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deception level retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Deception level not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<DeceptionLevelDto>> getDeceptionLevelById(
            @Parameter(description = "Deception level ID") @PathVariable String id
    );

    @Operation(summary = "Create deception level")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Deception level created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<DeceptionLevelDto>> createDeceptionLevel(
            @Valid @RequestBody DeceptionLevelCreateRequest request
    );

    @Operation(summary = "Update deception level")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deception level updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Deception level not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<DeceptionLevelDto>> updateDeceptionLevel(
            @Parameter(description = "Deception level ID") @PathVariable String id,
            @Valid @RequestBody DeceptionLevelUpdateRequest request
    );

    @Operation(summary = "Delete deception level")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deception level deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Deception level not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteDeceptionLevel(
            @Parameter(description = "Deception level ID") @PathVariable String id
    );
}
