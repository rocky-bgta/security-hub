package com.aspire.asat.billing.controller;

import com.aspire.asat.billing.constant.WebApiUrlConstants;
import com.aspire.asat.billing.dto.apiResponses.AllResponseDto;
import com.aspire.asat.billing.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.billing.dto.CommissionRateRequestDTO;
import com.aspire.asat.billing.dto.CommissionRateResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.COMMISSION_API, produces = "application/json")
@Tag(name = "Commission Management", description = "Endpoints for Commission Rate Management")
public interface CommissionRateController {

    @Operation(
            summary = "Create a commission rate",
            description = "Creates a new commission rate for a client if one does not already exist."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Commission rate created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or validation failed"),
            @ApiResponse(responseCode = "409", description = "Conflict: Commission rate already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(WebApiUrlConstants.CREATE)
    ResponseEntity<ApiResponseDto<CommissionRateResponseDTO>> createCommissionRate(
            @NotNull @Valid @RequestBody CommissionRateRequestDTO request);


    @Operation(summary = "Update a commission rate", description = "Updates the commission rate for an existing client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commission rate updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Commission rate not found for the specified client"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/update/{clientId}")
    ResponseEntity<ApiResponseDto<CommissionRateResponseDTO>> updateCommissionRate(
            @PathVariable("clientId") @NotBlank String clientId,
            @Valid @RequestBody CommissionRateRequestDTO request);


    @Operation(summary = "Get commission rate by client ID", description = "Fetches commission rate details for a specific client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commission rate retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No commission rate found for the client")
    })
    @GetMapping("/client/{clientId}")
    ResponseEntity<ApiResponseDto<CommissionRateResponseDTO>> getCommissionRateByClientId(
            @PathVariable("clientId") @NotBlank String clientId);


    @Operation(summary = "List all commission rates", description = "Lists all client commission rates with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commission rates listed successfully")
    })
    @GetMapping("/list")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<CommissionRateResponseDTO>>>> listCommissionRates(
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) int limit);


    @Operation(summary = "Delete commission rate", description = "Deletes a commission rate for a specific client")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Commission rate deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Commission rate not found for the client")
    })
    @DeleteMapping("/delete/{clientId}")
    ResponseEntity<ApiResponseDto<Void>> deleteCommissionRate(
            @PathVariable("clientId") @NotBlank String clientId);

}
