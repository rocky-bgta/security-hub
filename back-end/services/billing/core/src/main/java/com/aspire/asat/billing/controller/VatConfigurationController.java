package com.aspire.asat.billing.controller;

import com.aspire.asat.billing.constant.WebApiUrlConstants;
import com.aspire.asat.billing.dto.apiResponses.AllResponseDto;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.dto.VatConfigurationCreateDTO;
import com.aspire.asat.billing.dto.VatConfigurationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.VAT_API, produces = "application/json")
@Tag(name = "VAT Configuration", description = "Endpoints for VAT configuration management")
public interface VatConfigurationController {

    @Operation(summary = "Create VAT config", description = "Creates a new VAT configuration for a country")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "VAT configuration created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or validation failed"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/create")
    ResponseEntity<ApiResponseDto<VatConfigurationResponseDTO>> createVat(
            @Valid @RequestBody VatConfigurationCreateDTO dto);

    @Operation(summary = "Update VAT config", description = "Updates an existing VAT configuration for a country")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "VAT configuration updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "VAT configuration not found for the specified countryId"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/update/{countryId}")
    ResponseEntity<ApiResponseDto<VatConfigurationResponseDTO>> updateVat(
            @PathVariable @NotBlank String countryId,
            @Valid @RequestBody VatConfigurationCreateDTO dto);

    @Operation(summary = "Get VAT config by country ID", description = "Retrieves VAT configuration details for a specific country using countryId (UUID)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "VAT configuration retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "VAT configuration not found for the countryId")
    })
    @GetMapping("/country/{countryId}")
    ResponseEntity<ApiResponseDto<VatConfigurationResponseDTO>> getVatByCountry(
            @PathVariable @NotBlank String countryId);

    @Operation(summary = "Get all VAT configurations", description = "Lists all VAT configurations with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "VAT configurations retrieved successfully")
    })
    @GetMapping("/all")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<VatConfigurationResponseDTO>>>> getAll(
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int limit);

    @Operation(summary = "Delete VAT config by country ID", description = "Deletes a VAT configuration for a specific country using countryId (UUID)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "VAT configuration deleted successfully"),
            @ApiResponse(responseCode = "404", description = "VAT configuration not found for the countryId")
    })
    @DeleteMapping("/delete/{countryId}")
    ResponseEntity<ApiResponseDto<Void>> delete(
            @PathVariable @NotBlank String countryId);
}
