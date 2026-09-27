package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.mspUser.response.ClientLicenseSummaryDto;
import com.aspire.asat.registration.data.mspUser.response.ClientLicenseUsageResponseDto;
import com.aspire.asat.registration.data.mspUser.response.MspClientLicenseDetailResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MSP Client License Management", description = "MSP Admin view and export of client (Client Admin) license allocations")
@RequestMapping(value = WebApiUrlConstants.MSP_API + WebApiUrlConstants.MSP_CLIENT_LICENSES, produces = "application/json")
public interface MspClientLicenseController {

    @Operation(summary = "List client licenses", description = "Paginated list of Client Admins for the MSP with license summary (allocated, in use, remaining, expiry)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client license list retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ClientLicenseSummaryDto>>>> getClientLicenseList(
            @Parameter(description = "MSP ID", required = true) @RequestParam("mspId") @NotBlank String mspId,
            @Parameter(description = "Page offset (default: 0)") @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @Parameter(description = "Page size (default: 10)") @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @Parameter(description = "Search by client name or email") @RequestParam(value = "search", required = false) String search);

    @Operation(summary = "View client license details", description = "Detailed license information for a single Client Admin (products, allocated, in use, remaining)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client license details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client admin not found or does not belong to MSP"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/clients/{clientAdminId}")
    ResponseEntity<ApiResponseDto<MspClientLicenseDetailResponseDto>> getClientLicenseDetail(
            @Parameter(description = "MSP ID", required = true) @RequestParam("mspId") @NotBlank String mspId,
            @Parameter(description = "Client Admin ID", required = true) @PathVariable("clientAdminId") @NotBlank String clientAdminId);

    @Operation(summary = "View client license usage", description = "License usage for a client: in use, history, inactive count, upcoming expirations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client license usage retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client admin not found or does not belong to MSP"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/clients/{clientAdminId}/usage")
    ResponseEntity<ApiResponseDto<ClientLicenseUsageResponseDto>> getClientLicenseUsage(
            @Parameter(description = "MSP ID", required = true) @RequestParam("mspId") @NotBlank String mspId,
            @Parameter(description = "Client Admin ID", required = true) @PathVariable("clientAdminId") @NotBlank String clientAdminId);

    @Operation(summary = "Export client licenses", description = "Export client license data for the MSP as CSV, Excel, or PDF")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Export file generated successfully"),
            @ApiResponse(responseCode = "404", description = "MSP not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/export")
    ResponseEntity<org.springframework.core.io.Resource> exportClientLicenses(
            @Parameter(description = "MSP ID", required = true) @RequestParam("mspId") @NotBlank String mspId,
            @Parameter(description = "Export format: csv, xlsx, pdf (default: csv)") @RequestParam(value = "format", defaultValue = "csv", required = false) String format);
}
