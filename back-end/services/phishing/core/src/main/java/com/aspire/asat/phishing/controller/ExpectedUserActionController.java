package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.ExpectedUserActionCreateRequest;
import com.aspire.asat.phishing.dto.request.ExpectedUserActionUpdateRequest;
import com.aspire.asat.phishing.dto.response.ExpectedUserActionDto;
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
 * Controller interface for configurable expected user action catalog.
 */
@Tag(name = "Expected user actions", description = "APIs for managing configurable expected user actions")
@RequestMapping(value = WebApiUrlConstants.EXPECTED_USER_ACTIONS_PATH, produces = "application/json")
public interface ExpectedUserActionController {

    @Operation(summary = "Get list of expected user actions",
            description = "Retrieve paginated list with optional name search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expected user actions retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ExpectedUserActionDto>>>> getExpectedUserActions(
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

    @Operation(summary = "Get expected user action by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expected user action retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Expected user action not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<ExpectedUserActionDto>> getExpectedUserActionById(
            @Parameter(description = "Expected user action ID") @PathVariable String id
    );

    @Operation(summary = "Create expected user action")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Expected user action created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<ExpectedUserActionDto>> createExpectedUserAction(
            @Valid @RequestBody ExpectedUserActionCreateRequest request
    );

    @Operation(summary = "Update expected user action")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expected user action updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Expected user action not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<ExpectedUserActionDto>> updateExpectedUserAction(
            @Parameter(description = "Expected user action ID") @PathVariable String id,
            @Valid @RequestBody ExpectedUserActionUpdateRequest request
    );

    @Operation(summary = "Delete expected user action")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expected user action deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Expected user action not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteExpectedUserAction(
            @Parameter(description = "Expected user action ID") @PathVariable String id
    );
}
