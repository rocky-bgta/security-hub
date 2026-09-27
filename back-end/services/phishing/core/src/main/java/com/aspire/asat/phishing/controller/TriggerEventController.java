package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.TriggerEventCreateRequest;
import com.aspire.asat.phishing.dto.request.TriggerEventUpdateRequest;
import com.aspire.asat.phishing.dto.response.TriggerEventDto;
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
 * Controller interface for configurable trigger event catalog.
 */
@Tag(name = "Trigger events", description = "APIs for managing configurable trigger events")
@RequestMapping(value = WebApiUrlConstants.TRIGGER_EVENTS_PATH, produces = "application/json")
public interface TriggerEventController {

    @Operation(summary = "Get list of trigger events",
            description = "Retrieve paginated list with optional name search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trigger events retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<TriggerEventDto>>>> getTriggerEvents(
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

    @Operation(summary = "Get trigger event by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trigger event retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Trigger event not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<TriggerEventDto>> getTriggerEventById(
            @Parameter(description = "Trigger event ID") @PathVariable String id
    );

    @Operation(summary = "Create trigger event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trigger event created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<TriggerEventDto>> createTriggerEvent(
            @Valid @RequestBody TriggerEventCreateRequest request
    );

    @Operation(summary = "Update trigger event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trigger event updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Trigger event not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<TriggerEventDto>> updateTriggerEvent(
            @Parameter(description = "Trigger event ID") @PathVariable String id,
            @Valid @RequestBody TriggerEventUpdateRequest request
    );

    @Operation(summary = "Delete trigger event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trigger event deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Trigger event not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteTriggerEvent(
            @Parameter(description = "Trigger event ID") @PathVariable String id
    );
}
