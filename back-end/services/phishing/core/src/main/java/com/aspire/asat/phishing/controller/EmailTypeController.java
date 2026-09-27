package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.EmailTypeCreateRequest;
import com.aspire.asat.phishing.dto.response.EmailTypeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller interface for EmailType configuration.
 */
@Tag(name = "Email Types", description = "APIs for managing phishing email type configuration")
@RequestMapping(value = WebApiUrlConstants.EMAIL_TYPES_PATH, produces = "application/json")
public interface EmailTypeController {

    @Operation(summary = "Get list of email types",
            description = "Retrieve paginated list of email types with optional search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email types retrieved successfully")
    })
    @GetMapping
    ResponseEntity<AllResponseDto<List<EmailTypeDto>>> getEmailTypes(
            @Parameter(description = "Optional search term (matches name)")
            @RequestParam(required = false) String searchParam,

            @Parameter(description = "Filter by active entries")
            @RequestParam(defaultValue = "true") boolean isActive,

            @Parameter(description = "Page offset (0-based, in items)")
            @RequestParam(defaultValue = "0") int offset,

            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int pageSize,

            @Parameter(description = "Field to sort by (name, createdAt)")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortOrder
    );

    @Operation(summary = "Get email type by ID", description = "Retrieve a specific email type by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email type retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Email type not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<EmailTypeDto>> getEmailTypeById(
            @Parameter(description = "Email type ID")
            @PathVariable String id
    );

    @Operation(summary = "Create email type", description = "Create a new email type configuration entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Email type created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<EmailTypeDto>> createEmailType(
            @Valid @RequestBody EmailTypeCreateRequest request
    );
}

