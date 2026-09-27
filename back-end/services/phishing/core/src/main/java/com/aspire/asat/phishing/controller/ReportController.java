package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * Controller interface for report endpoints.
 */
@Tag(name = "Reports", description = "APIs for generating and exporting reports")
@RequestMapping(value = WebApiUrlConstants.REPORTS_PATH)
public interface ReportController {

    @Operation(summary = "Get campaign reports list")
    @GetMapping("/campaigns")
    ResponseEntity<AllResponseDto<List<CampaignPerformanceDto>>> getCampaignReports(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

    @Operation(summary = "Get detailed campaign report")
    @GetMapping("/campaigns/{campaignId}")
    ResponseEntity<ApiResponseDto<CampaignPerformanceDto>> getCampaignReportById(
            @PathVariable String campaignId
    );

    @Operation(summary = "Export campaign report", 
               description = "Export campaign report as PDF or Excel")
    @GetMapping("/campaigns/{campaignId}/export")
    ResponseEntity<byte[]> exportCampaignReport(
            @PathVariable String campaignId,
            @Parameter(description = "Export format: pdf, excel, csv")
            @RequestParam(defaultValue = "csv") String format
    );

    @Operation(summary = "Get email activity log")
    @GetMapping("/email-activity")
    ResponseEntity<AllResponseDto<List<EmailActivityDto>>> getEmailActivityLog(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) ActivityType activityType,
            @RequestParam(required = false) Instant startTime,
            @RequestParam(required = false) Instant endTime,
            @Parameter(description = "Optional search across recipient name, recipient email, and campaign name")
            @RequestParam(required = false) String search,
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

    @Operation(summary = "Get user risk report")
    @GetMapping("/user-risk")
    ResponseEntity<AllResponseDto<List<UserRiskSummaryDto>>> getUserRiskReport(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) RiskLevel riskLevel,
            @RequestParam(defaultValue = "riskScore") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

    @Operation(summary = "Export user risk report")
    @GetMapping("/user-risk/export")
    ResponseEntity<byte[]> exportUserRiskReport(
            @Parameter(description = "Export format: pdf, excel, csv")
            @RequestParam(defaultValue = "csv") String format,
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );

    @Operation(summary = "Get breach summary report")
    @GetMapping("/breach-summary")
    ResponseEntity<ApiResponseDto<BreachSummaryDto>> getBreachSummaryReport(
            @Parameter(description = "Simulation channel (EMAIL, SMS, VOICE). Defaults to EMAIL.")
            @RequestParam(defaultValue = "EMAIL") CampaignChannel channel
    );
}
