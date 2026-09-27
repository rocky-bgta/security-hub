package com.aspire.asat.breachdetection.controller;

import com.aspire.asat.breachdetection.constant.WebApiUrlConstants;
import com.aspire.asat.breachdetection.dto.AllResponseDto;
import com.aspire.asat.breachdetection.dto.ApiResponseDto;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertSeverity;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertStatus;
import com.aspire.asat.breachdetection.dto.enums.ShodanAlertType;
import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
import com.aspire.asat.breachdetection.dto.request.AddShodanMonitorRequest;
import com.aspire.asat.breachdetection.dto.response.ImpersonationTacticsSummaryDto;
import com.aspire.asat.breachdetection.dto.response.ShodanAlertDto;
import com.aspire.asat.breachdetection.dto.response.ShodanAlertTableRowDto;
import com.aspire.asat.breachdetection.dto.response.ShodanMonitorDto;
import com.aspire.asat.breachdetection.dto.response.ShodanOverviewDto;
import com.aspire.asat.breachdetection.dto.response.ShodanRiskTrendResponseDto;
import com.aspire.asat.breachdetection.dto.response.ShodanSyncResultDto;
import com.aspire.asat.breachdetection.dto.response.ShodanTacticDistributionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * REST endpoints backing the "IP / Domain Breach Detection & Mitigation" UI.
 * All endpoints are scoped to the current client via {@code CurrentContext}.
 */
@Tag(name = "Shodan Breach Detection for IPs & Domains",
        description = "Shodan-backed IP & Domain breach monitoring (alerts, trends, tactics).")
@RequestMapping(value = WebApiUrlConstants.SHODAN_API)
public interface ShodanBreachController {

    // --- Monitor CRUD ----------------------------------------------------

    @Operation(summary = "Add an IP or domain to monitor",
            description = "Registers a subject (IP address or domain) for Shodan-backed monitoring.")
    @PostMapping("/monitors")
    ResponseEntity<ApiResponseDto<ShodanMonitorDto>> addMonitor(
            @RequestBody @Valid AddShodanMonitorRequest request);

    @Operation(summary = "Delete a monitored subject")
    @DeleteMapping("/monitors/{id}")
    ResponseEntity<ApiResponseDto<Void>> deleteMonitor(@PathVariable String id);

    @Operation(summary = "List monitored subjects for a given type")
    @GetMapping("/monitors")
    ResponseEntity<ApiResponseDto<List<ShodanMonitorDto>>> listMonitors(
            @RequestParam ShodanSubjectType subjectType);

    // --- Sync ------------------------------------------------------------

    @Operation(summary = "Trigger a manual Shodan sync",
            description = "Runs a synchronous Shodan sync for all monitored subjects of the given type.")
    @PostMapping("/sync")
    ResponseEntity<ApiResponseDto<ShodanSyncResultDto>> triggerSync(
            @RequestParam ShodanSubjectType subjectType);

    // --- Dashboard reads -------------------------------------------------

    @Operation(summary = "Overview stats (Number of subjects, Total breaches, Last scan)")
    @GetMapping("/overview")
    ResponseEntity<ApiResponseDto<ShodanOverviewDto>> getOverview(
            @RequestParam ShodanSubjectType subjectType);

    @Operation(summary = "List IP / Domain breach alerts (dashboard table)")
    @GetMapping("/alerts")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ShodanAlertDto>>>> getAlerts(
            @RequestParam ShodanSubjectType subjectType,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "dateDetected") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) ShodanAlertStatus status,
            @RequestParam(required = false) ShodanAlertSeverity severity,
            @RequestParam(required = false) ShodanAlertType alertType
    );

    @Operation(summary = "List IP / Domain breach alerts (frontend table-friendly payload)")
    @GetMapping("/alerts/table")
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ShodanAlertTableRowDto>>>> getAlertsTable(
            @RequestParam ShodanSubjectType subjectType,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(defaultValue = "dateDetected") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) ShodanAlertStatus status,
            @RequestParam(required = false) ShodanAlertSeverity severity,
            @RequestParam(required = false) ShodanAlertType alertType
    );

    @Operation(summary = "Get a single alert by id")
    @GetMapping("/alerts/{id}")
    ResponseEntity<ApiResponseDto<ShodanAlertDto>> getAlertById(@PathVariable String id);

    @Operation(summary = "Update alert status (OPEN / ACKNOWLEDGED / RESOLVED)")
    @PatchMapping("/alerts/{id}/status")
    ResponseEntity<ApiResponseDto<ShodanAlertDto>> updateAlertStatus(
            @PathVariable String id,
            @RequestParam ShodanAlertStatus status);

    @Operation(summary = "Risk Trend timeseries (last 30 / 60 / 90 days)",
            description = "Daily counts of alerts, broken down by severity (low / medium / high / critical). "
                    + "Allowed values for `days`: 30, 60, 90. Defaults to 30.")
    @GetMapping("/risk-trend")
    ResponseEntity<ApiResponseDto<ShodanRiskTrendResponseDto>> getRiskTrend(
            @RequestParam ShodanSubjectType subjectType,
            @RequestParam(defaultValue = "30") int days);

    @Operation(summary = "Tactic Distribution for the pie chart (Phishing / Social / Spoofed Email / Typosquatting)")
    @GetMapping("/tactic-distribution")
    ResponseEntity<ApiResponseDto<ShodanTacticDistributionDto>> getTacticDistribution(
            @RequestParam ShodanSubjectType subjectType);

    @Operation(summary = "Impersonation Tactics summary (side panel counts)")
    @GetMapping("/impersonation-tactics")
    ResponseEntity<ApiResponseDto<ImpersonationTacticsSummaryDto>> getImpersonationTactics(
            @RequestParam ShodanSubjectType subjectType);
}
