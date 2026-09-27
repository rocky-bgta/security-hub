package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.DataCaptureTypeCreateRequest;
import com.aspire.asat.phishing.dto.request.DataCaptureTypeUpdateRequest;
import com.aspire.asat.phishing.dto.response.DataCaptureTypeDto;
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
 * Controller interface for configurable data capture type catalog.
 */
@Tag(name = "Data capture types", description = "APIs for managing phishing data capture type configuration")
@RequestMapping(value = WebApiUrlConstants.DATA_CAPTURE_TYPES_PATH, produces = "application/json")
public interface DataCaptureTypeController {

    @Operation(summary = "Get list of data capture types",
            description = "Retrieve paginated list with optional name search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data capture types retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<DataCaptureTypeDto>>>> getDataCaptureTypes(
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

    @Operation(summary = "Get data capture type by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data capture type retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Data capture type not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<DataCaptureTypeDto>> getDataCaptureTypeById(
            @Parameter(description = "Data capture type ID") @PathVariable String id
    );

    @Operation(summary = "Create data capture type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Data capture type created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<DataCaptureTypeDto>> createDataCaptureType(
            @Valid @RequestBody DataCaptureTypeCreateRequest request
    );

    @Operation(summary = "Update data capture type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data capture type updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Data capture type not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<DataCaptureTypeDto>> updateDataCaptureType(
            @Parameter(description = "Data capture type ID") @PathVariable String id,
            @Valid @RequestBody DataCaptureTypeUpdateRequest request
    );

    @Operation(summary = "Delete data capture type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data capture type deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Data capture type not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteDataCaptureType(
            @Parameter(description = "Data capture type ID") @PathVariable String id
    );
}
