package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.ToneCreateRequest;
import com.aspire.asat.phishing.dto.request.ToneUpdateRequest;
import com.aspire.asat.phishing.dto.response.ToneDto;
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
 * Controller interface for configurable tone catalog.
 */
@Tag(name = "Tones", description = "APIs for managing phishing tone configuration")
@RequestMapping(value = WebApiUrlConstants.TONES_PATH, produces = "application/json")
public interface ToneController {

    @Operation(summary = "Get list of tones",
            description = "Retrieve paginated list of tones with optional name search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tones retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ToneDto>>>> getTones(
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

    @Operation(summary = "Get tone by ID", description = "Retrieve a specific tone by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tone retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Tone not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<ToneDto>> getToneById(
            @Parameter(description = "Tone ID") @PathVariable String id
    );

    @Operation(summary = "Create tone", description = "Create a new tone configuration entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tone created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<ToneDto>> createTone(
            @Valid @RequestBody ToneCreateRequest request
    );

    @Operation(summary = "Update tone", description = "Replace an existing tone entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tone updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Tone not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<ToneDto>> updateTone(
            @Parameter(description = "Tone ID") @PathVariable String id,
            @Valid @RequestBody ToneUpdateRequest request
    );

    @Operation(summary = "Delete tone", description = "Delete a tone configuration entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tone deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Tone not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteTone(
            @Parameter(description = "Tone ID") @PathVariable String id
    );
}
