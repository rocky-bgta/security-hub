package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.netTerm.CreateNetTermConfigurationRequestDTO;
import com.aspire.asat.registration.data.netTerm.NetTermConfigurationDropdownDTO;
import com.aspire.asat.registration.data.netTerm.NetTermConfigurationResponseDTO;
import com.aspire.asat.registration.data.netTerm.UpdateNetTermConfigurationRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Net Term Configuration", description = "Manage net term configurations for payment terms")
@RequestMapping(value = "/api/net-term-configuration", produces = "application/json")
public interface NetTermConfigurationController {

    @Operation(summary = "Create a new net term configuration")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Net term configuration created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<NetTermConfigurationResponseDTO>> createNetTerm(
            @Valid @RequestBody CreateNetTermConfigurationRequestDTO dto
    );

    @Operation(summary = "Get all net term configurations with pagination and filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Net term configurations retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<NetTermConfigurationResponseDTO>>>> getAllNetTerms(
            @RequestParam(value = "search", required = false)
            @Parameter(description = "Search by net term name (partial match)") String search,

            @RequestParam(value = "isActive", required = false)
            @Parameter(description = "Filter by active status (true for active, false for inactive)") Boolean isActive,

            @RequestParam(value = "offset", defaultValue = "0")
            @Parameter(description = "Pagination offset") int offset,

            @RequestParam(value = "limit", defaultValue = "10")
            @Parameter(description = "Number of items to return") int limit
    );

    @Operation(summary = "Get net term configuration by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Net term configuration fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Net term configuration not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<NetTermConfigurationResponseDTO>> getNetTermById(
            @PathVariable("id") @Parameter(description = "Net term configuration ID") String id
    );

    @Operation(summary = "Update an existing net term configuration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Net term configuration updated successfully"),
            @ApiResponse(responseCode = "404", description = "Net term configuration not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<NetTermConfigurationResponseDTO>> updateNetTerm(
            @PathVariable("id") @Parameter(description = "Net term configuration ID") String id,
            @Valid @RequestBody UpdateNetTermConfigurationRequestDTO dto
    );

    @Operation(summary = "Delete a net term configuration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Net term configuration deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Net term configuration not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteNetTerm(
            @PathVariable("id") @Parameter(description = "Net term configuration ID") String id
    );

    @Operation(summary = "Get all active net term configurations for dropdown")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active net term configurations retrieved successfully")
    })
    @GetMapping("/active")
    ResponseEntity<ApiResponseDto<List<NetTermConfigurationDropdownDTO>>> getAllActiveNetTerms();
}

