package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.DifficultyCreateRequest;
import com.aspire.asat.phishing.dto.request.DifficultyUpdateRequest;
import com.aspire.asat.phishing.dto.response.DifficultyDto;
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
 * Controller interface for configurable difficulty catalog.
 */
@Tag(name = "Difficulties", description = "APIs for managing configurable difficulty levels")
@RequestMapping(value = WebApiUrlConstants.DIFFICULTIES_PATH, produces = "application/json")
public interface DifficultyController {

    @Operation(summary = "Get list of difficulties",
            description = "Retrieve paginated list with optional name search")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Difficulties retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<DifficultyDto>>>> getDifficulties(
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

    @Operation(summary = "Get difficulty by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Difficulty retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Difficulty not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<DifficultyDto>> getDifficultyById(
            @Parameter(description = "Difficulty ID") @PathVariable String id
    );

    @Operation(summary = "Create difficulty")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Difficulty created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<DifficultyDto>> createDifficulty(
            @Valid @RequestBody DifficultyCreateRequest request
    );

    @Operation(summary = "Update difficulty")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Difficulty updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Difficulty not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<DifficultyDto>> updateDifficulty(
            @Parameter(description = "Difficulty ID") @PathVariable String id,
            @Valid @RequestBody DifficultyUpdateRequest request
    );

    @Operation(summary = "Delete difficulty")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Difficulty deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Difficulty not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteDifficulty(
            @Parameter(description = "Difficulty ID") @PathVariable String id
    );
}
