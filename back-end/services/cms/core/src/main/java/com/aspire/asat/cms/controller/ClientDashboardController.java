package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.clientDashboard.ClientDashboardRequestDto;
import com.aspire.asat.cms.dto.clientDashboard.ClientDashboardResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Client Dashboard Management", description = "APIs for managing client dashboard data")
@RequestMapping(value = WebApiUrlConstants.CLIENT_DASHBOARD_API, produces = "application/json")
public interface ClientDashboardController {

    @PostMapping(consumes = "application/json")
    @Operation(summary = "Create or update client dashboard", description = "Creates new client dashboard data if clientAdminId doesn't exist, otherwise updates existing data")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client dashboard created or updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<ClientDashboardResponseDto>> createOrUpdateClientDashboard(
            @Valid @RequestBody ClientDashboardRequestDto requestDto
    );

    @GetMapping("/client-admin")
    @Operation(summary = "Get client dashboard by client admin ID", description = "Retrieves client dashboard data for a specific client admin")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client dashboard retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Client dashboard not found"),
            @ApiResponse(responseCode = "400", description = "Invalid client admin ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<ClientDashboardResponseDto>> getClientDashboardByClientAdminId();

}
