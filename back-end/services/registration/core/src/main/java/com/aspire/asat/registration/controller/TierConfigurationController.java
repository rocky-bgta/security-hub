package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.tierManagement.CreateTierConfigurationRequestDTO;
import com.aspire.asat.registration.data.tierManagement.TierConfigurationResponseDTO;
import com.aspire.asat.registration.data.tierManagement.UpdateTierConfigurationRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tier Configuration", description = "Manage commission tiers based on sales performance")
@RequestMapping(value = "/api/tier", produces = "application/json")
public interface TierConfigurationController {

    @Operation(summary = "Create a new tier configuration")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tier created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
    })
    @PostMapping
    ResponseEntity<ApiResponseDto<TierConfigurationResponseDTO>> createTier(
            @Valid @RequestBody CreateTierConfigurationRequestDTO dto
    );

    @Operation(summary = "Get all tier configurations with pagination and filters")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tier list retrieved successfully")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<TierConfigurationResponseDTO>>>> getAllTiers(
            @RequestParam(value = "search", required = false)
            @Parameter(description = "Search by tier name (partial match)") String search,

            @RequestParam(value = "status", required = false)
            @Parameter(description = "Filter by status (true for active, false for inactive)") Boolean status,

            @RequestParam(value = "offset", defaultValue = "0")
            @Parameter(description = "Pagination offset") int offset,

            @RequestParam(value = "limit", defaultValue = "10")
            @Parameter(description = "Number of items to return") int limit
    );

    @Operation(summary = "Get Tier Configuration by MSP ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tier configuration fetched successfully"),
            @ApiResponse(responseCode = "404", description = "MSP or Tier configuration not found")
    })
    @GetMapping("/by-msp")
    ResponseEntity<ApiResponseDto<TierConfigurationResponseDTO>> getTierByMspId(
            @RequestParam("mspId") @Parameter(description = "MSP ID") String mspId
    );

    @Operation(summary = "Get Tier Configuration by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tier configuration fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Tier configuration not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ApiResponseDto<TierConfigurationResponseDTO>> getTierById(
            @PathVariable("id") @Parameter(description = "Tier Configuration ID") String id
    );

    @Operation(summary = "Update Tier Configuration")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tier configuration updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Tier configuration not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<ApiResponseDto<TierConfigurationResponseDTO>> updateTier(
            @PathVariable("id") @Parameter(description = "Tier Configuration ID") String id,
            @Valid @RequestBody UpdateTierConfigurationRequestDTO dto
    );

}
