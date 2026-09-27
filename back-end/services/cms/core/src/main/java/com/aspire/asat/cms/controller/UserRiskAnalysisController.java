package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.clientDashboard.UserRiskAnalysisResponseDto;
import com.aspire.asat.cms.dto.clientDashboard.UserRiskDetailDto;
import com.aspire.asat.cms.dto.enums.RiskCategory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller interface for User Risk Analysis APIs
 * Provides risk-based analysis of users for client dashboard
 */
@Tag(name = "User Risk Analysis", description = "APIs for user risk-based analysis and reporting")
@RequestMapping("/api/v1/client-dashboard")
public interface UserRiskAnalysisController {

    @GetMapping("/risk-analysis")
    @Operation(
        summary = "Get user risk analysis report", 
        description = "Generates a comprehensive risk analysis report for all users under the current client admin based on their training completion progress. Optionally filter by productId."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Risk analysis report generated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing user context"),
        @ApiResponse(responseCode = "404", description = "Client admin not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<UserRiskAnalysisResponseDto>> getUserRiskAnalysis(
        @Parameter(description = "Optional product ID to filter by. If not provided, analyzes all products")
        @RequestParam(value = "productId", required = false) String productId
    );

    @GetMapping("/risk-analysis/details")
    @Operation(
        summary = "Get detailed user risk analysis", 
        description = "Returns detailed risk analysis for individual users under the current client admin with their progress details"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detailed risk analysis retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing user context"),
        @ApiResponse(responseCode = "404", description = "Client admin not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<List<UserRiskDetailDto>>> getUserRiskAnalysisDetails(
        @Parameter(description = "Risk category filter (SAFE, LOW_RISK, AVERAGE_RISK, HIGH_RISK)")
        @RequestParam(value = "riskCategory", required = false) RiskCategory riskCategory,
        
        @Parameter(description = "Page offset (default: 0)")
        @RequestParam(value = "offset", defaultValue = "0") Integer offset,
        
        @Parameter(description = "Page size (default: 50)")
        @RequestParam(value = "pageSize", defaultValue = "50") Integer pageSize
    );

    @GetMapping("/risk-analysis/export")
    @Operation(
        summary = "Export user risk analysis report", 
        description = "Exports the user risk analysis report as CSV for download"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Risk analysis report exported successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing user context"),
        @ApiResponse(responseCode = "404", description = "Client admin not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<String>> exportUserRiskAnalysis();
}
