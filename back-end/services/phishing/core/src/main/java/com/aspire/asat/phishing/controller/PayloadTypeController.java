package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.PayloadTypeChannel;
import com.aspire.asat.phishing.dto.request.PayloadTypeCreateRequest;
import com.aspire.asat.phishing.dto.request.PayloadTypeUpdateRequest;
import com.aspire.asat.phishing.dto.response.PayloadTypeDto;
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
 * Controller interface for configurable payload type catalog.
 */
@Tag(name = "Payload Types", description = "APIs for managing phishing payload type configuration")
@RequestMapping(value = WebApiUrlConstants.PAYLOAD_TYPES_PATH, produces = "application/json")
public interface PayloadTypeController {

    @Operation(summary = "Get list of payload types",
            description = "Retrieve paginated list of payload types with optional name search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payload types retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<PayloadTypeDto>>>> getPayloadTypes(
            @Parameter(description = "Optional search term (matches name, case-insensitive substring)")
            @RequestParam(required = false) String searchParam,

            @Parameter(description = "Filter by active entries")
            @RequestParam(defaultValue = "true") boolean isActive,

            @Parameter(description = "Page index (0-based), same semantics as email-templates list")
            @RequestParam(defaultValue = "0") int offset,

            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int pageSize,

            @Parameter(description = "Field to sort by (displayOrder, name, createdAt)")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortOrder,

            @Parameter(description = "Delivery channel filter (EMAIL, SMS, or VOICE). Defaults to EMAIL.")
            @RequestParam(required = false, defaultValue = "EMAIL") PayloadTypeChannel channel
    );

    @Operation(summary = "Get payload type by ID", description = "Retrieve a specific payload type by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payload type retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Payload type not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<PayloadTypeDto>> getPayloadTypeById(
            @Parameter(description = "Payload type ID")
            @PathVariable String id
    );

    @Operation(summary = "Create payload type", description = "Create a new payload type configuration entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payload type created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<PayloadTypeDto>> createPayloadType(
            @Valid @RequestBody PayloadTypeCreateRequest request
    );

    @Operation(summary = "Update payload type", description = "Replace an existing payload type entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payload type updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Payload type not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<PayloadTypeDto>> updatePayloadType(
            @Parameter(description = "Payload type ID") @PathVariable String id,
            @Valid @RequestBody PayloadTypeUpdateRequest request
    );

    @Operation(summary = "Delete payload type", description = "Delete a payload type configuration entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payload type deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Payload type not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deletePayloadType(
            @Parameter(description = "Payload type ID") @PathVariable String id
    );
}
