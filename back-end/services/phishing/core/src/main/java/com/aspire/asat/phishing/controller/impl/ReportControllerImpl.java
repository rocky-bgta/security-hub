package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.ReportController;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.dto.response.*;
import com.aspire.asat.phishing.service.DashboardService;
import com.aspire.asat.phishing.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * Controller implementation for report endpoints.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ReportControllerImpl implements ReportController {

    private final ReportService reportService;
    private final DashboardService dashboardService;

    @Override
    public ResponseEntity<AllResponseDto<List<CampaignPerformanceDto>>> getCampaignReports(
            int offset, int pageSize, String sortBy, String sortOrder, CampaignChannel channel) {
        try {
            List<CampaignPerformanceDto> reports = reportService.getCampaignReports(
                    offset, pageSize, sortBy, sortOrder, channel);
            long totalCount = reportService.countCampaignReports(channel);

            return ResponseEntity.ok(AllResponseDto.<List<CampaignPerformanceDto>>builder()
                    .items(reports)
                    .total(totalCount)
                    .offset(offset)
                    .pageSize(pageSize)
                    .build());
        } catch (Exception e) {
            log.error("Error getting campaign reports", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AllResponseDto.<List<CampaignPerformanceDto>>builder()
                            .items(List.of())
                            .total(0L)
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<CampaignPerformanceDto>> getCampaignReportById(String campaignId) {
        try {
            CampaignPerformanceDto report = reportService.getCampaignReportById(campaignId);
            return ResponseEntity.ok(ApiResponseDto.<CampaignPerformanceDto>builder()
                    .data(report)
                    .message("Campaign report retrieved successfully")
                    .build());
        } catch (RuntimeException e) {
            log.error("Error getting campaign report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.<CampaignPerformanceDto>builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<byte[]> exportCampaignReport(String campaignId, String format) {
        try {
            byte[] data = reportService.exportCampaignReport(campaignId, format);
            
            String contentType = "text/csv";
            String filename = "campaign-report-" + campaignId + ".csv";
            
            if ("excel".equalsIgnoreCase(format)) {
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                filename = "campaign-report-" + campaignId + ".xlsx";
            } else if ("pdf".equalsIgnoreCase(format)) {
                contentType = "application/pdf";
                filename = "campaign-report-" + campaignId + ".pdf";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(data);
        } catch (Exception e) {
            log.error("Error exporting campaign report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<AllResponseDto<List<EmailActivityDto>>> getEmailActivityLog(
            int offset, int pageSize, ActivityType activityType, Instant startTime, Instant endTime,
            String search, CampaignChannel channel) {
        try {
            List<EmailActivityDto> activities = reportService.getEmailActivityLog(
                    offset, pageSize, activityType, startTime, endTime, search, channel);
            long totalCount = reportService.countEmailActivities(
                    activityType, startTime, endTime, search, channel);

            return ResponseEntity.ok(AllResponseDto.<List<EmailActivityDto>>builder()
                    .items(activities)
                    .total(totalCount)
                    .offset(offset)
                    .pageSize(pageSize)
                    .build());
        } catch (Exception e) {
            log.error("Error getting email activity log", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AllResponseDto.<List<EmailActivityDto>>builder()
                            .items(List.of())
                            .total(0L)
                            .build());
        }
    }

    @Override
    public ResponseEntity<AllResponseDto<List<UserRiskSummaryDto>>> getUserRiskReport(
            int offset, int pageSize, String department, String search, RiskLevel riskLevel,
            String sortBy, String sortOrder, CampaignChannel channel) {
        try {
            List<UserRiskSummaryDto> users = reportService.getUserRiskReport(
                    offset, pageSize, department, search, riskLevel, sortBy, sortOrder, channel);
            long totalCount = reportService.countUsersForRiskReport(department, search, riskLevel, channel);

            return ResponseEntity.ok(AllResponseDto.<List<UserRiskSummaryDto>>builder()
                    .items(users)
                    .total(totalCount)
                    .offset(offset)
                    .pageSize(pageSize)
                    .build());
        } catch (Exception e) {
            log.error("Error getting user risk report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AllResponseDto.<List<UserRiskSummaryDto>>builder()
                            .items(List.of())
                            .total(0L)
                            .build());
        }
    }

    @Override
    public ResponseEntity<byte[]> exportUserRiskReport(String format, CampaignChannel channel) {
        try {
            byte[] data = reportService.exportUserRiskReport(format, channel);
            
            String contentType = "text/csv";
            String filename = "user-risk-report.csv";
            
            if ("excel".equalsIgnoreCase(format)) {
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                filename = "user-risk-report.xlsx";
            } else if ("pdf".equalsIgnoreCase(format)) {
                contentType = "application/pdf";
                filename = "user-risk-report.pdf";
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(data.length)
                    .body(data);
        } catch (Exception e) {
            log.error("Error exporting user risk report. format={}", format, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<BreachSummaryDto>> getBreachSummaryReport(CampaignChannel channel) {
        try {
            BreachSummaryDto summary = dashboardService.getBreachSummary(channel);
            return ResponseEntity.ok(ApiResponseDto.<BreachSummaryDto>builder()
                    .data(summary)
                    .message("Breach summary report retrieved successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error getting breach summary report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<BreachSummaryDto>builder()
                            .message("Failed to retrieve breach summary: " + e.getMessage())
                            .build());
        }
    }
}
