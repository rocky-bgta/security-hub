package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.ConstraintsDataCreateRequest;
import com.aspire.asat.phishing.dto.request.ConstraintsDataUpdateRequest;
import com.aspire.asat.phishing.dto.response.ConstraintsDataDto;
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
 * Controller interface for configurable constraints data catalog.
 */
@Tag(name = "Constraints data", description = "APIs for managing configurable constraints data")
@RequestMapping(value = WebApiUrlConstants.CONSTRAINTS_DATA_PATH, produces = "application/json")
public interface ConstraintsDataController {

    @Operation(summary = "Get list of constraints data entries",
            description = "Retrieve paginated list with optional name search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Constraints data retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ConstraintsDataDto>>>> getConstraintsData(
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

    @Operation(summary = "Get constraints data by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Constraints data retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Constraints data not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<ConstraintsDataDto>> getConstraintsDataById(
            @Parameter(description = "Constraints data ID") @PathVariable String id
    );

    @Operation(summary = "Create constraints data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Constraints data created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<ConstraintsDataDto>> createConstraintsData(
            @Valid @RequestBody ConstraintsDataCreateRequest request
    );

    @Operation(summary = "Update constraints data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Constraints data updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Constraints data not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<ConstraintsDataDto>> updateConstraintsData(
            @Parameter(description = "Constraints data ID") @PathVariable String id,
            @Valid @RequestBody ConstraintsDataUpdateRequest request
    );

    @Operation(summary = "Delete constraints data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Constraints data deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Constraints data not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteConstraintsData(
            @Parameter(description = "Constraints data ID") @PathVariable String id
    );
}
