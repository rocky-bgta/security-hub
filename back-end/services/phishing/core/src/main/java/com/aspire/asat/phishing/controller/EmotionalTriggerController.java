package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.EmotionalTriggerCreateRequest;
import com.aspire.asat.phishing.dto.request.EmotionalTriggerUpdateRequest;
import com.aspire.asat.phishing.dto.response.EmotionalTriggerDto;
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
 * Controller interface for configurable emotional trigger catalog.
 */
@Tag(name = "Emotional triggers", description = "APIs for managing configurable emotional triggers")
@RequestMapping(value = WebApiUrlConstants.EMOTIONAL_TRIGGERS_PATH, produces = "application/json")
public interface EmotionalTriggerController {

    @Operation(summary = "Get list of emotional triggers",
            description = "Retrieve paginated list with optional name search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emotional triggers retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<EmotionalTriggerDto>>>> getEmotionalTriggers(
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

    @Operation(summary = "Get emotional trigger by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emotional trigger retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Emotional trigger not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<EmotionalTriggerDto>> getEmotionalTriggerById(
            @Parameter(description = "Emotional trigger ID") @PathVariable String id
    );

    @Operation(summary = "Create emotional trigger")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Emotional trigger created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<EmotionalTriggerDto>> createEmotionalTrigger(
            @Valid @RequestBody EmotionalTriggerCreateRequest request
    );

    @Operation(summary = "Update emotional trigger")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emotional trigger updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Emotional trigger not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<EmotionalTriggerDto>> updateEmotionalTrigger(
            @Parameter(description = "Emotional trigger ID") @PathVariable String id,
            @Valid @RequestBody EmotionalTriggerUpdateRequest request
    );

    @Operation(summary = "Delete emotional trigger")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Emotional trigger deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Emotional trigger not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteEmotionalTrigger(
            @Parameter(description = "Emotional trigger ID") @PathVariable String id
    );
}
