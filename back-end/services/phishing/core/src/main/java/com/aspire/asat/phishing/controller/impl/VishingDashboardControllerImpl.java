package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.VishingDashboardController;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.SuccessKeywordsRequest;
import com.aspire.asat.phishing.dto.request.TeachableMomentRequest;
import com.aspire.asat.phishing.dto.request.VoiceTestCallRequest;
import com.aspire.asat.phishing.dto.response.*;
import com.aspire.asat.phishing.service.VishingDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VishingDashboardControllerImpl implements VishingDashboardController {

    private final VishingDashboardService vishingDashboardService;

    @Override
    public ResponseEntity<ApiResponseDto<VishingDataCaptureDto>> getDataCapture(
            String campaignId, int offset, int pageSize) {
        return ResponseEntity.ok(ApiResponseDto.<VishingDataCaptureDto>builder()
                .data(vishingDashboardService.getDataCapture(campaignId, offset, pageSize))
                .message("Data capture retrieved successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<String>>> getSuccessKeywords(String campaignId) {
        return ResponseEntity.ok(ApiResponseDto.<List<String>>builder()
                .data(vishingDashboardService.getSuccessKeywords(campaignId))
                .message("Success keywords retrieved successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<String>>> updateSuccessKeywords(
            String campaignId, SuccessKeywordsRequest request) {
        vishingDashboardService.updateSuccessKeywords(campaignId, request);
        return ResponseEntity.ok(ApiResponseDto.<List<String>>builder()
                .data(request.getKeywords())
                .message("Success keywords updated successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<VishingRemediationDto>> getRemediation(String campaignId) {
        return ResponseEntity.ok(ApiResponseDto.<VishingRemediationDto>builder()
                .data(vishingDashboardService.getRemediation(campaignId))
                .message("Remediation metrics retrieved successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> sendTeachableMoment(
            String campaignId, TeachableMomentRequest request) {
        vishingDashboardService.sendTeachableMoment(campaignId, request);
        return ResponseEntity.ok(ApiResponseDto.<String>builder()
                .data(request.getRecipientId())
                .message("Teachable moment sent")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<VishingReportDto>> getReport(String campaignId) {
        return ResponseEntity.ok(ApiResponseDto.<VishingReportDto>builder()
                .data(vishingDashboardService.getReport(campaignId))
                .message("Vishing report retrieved successfully")
                .build());
    }

    @Override
    public ResponseEntity<byte[]> exportReport(String campaignId, String format, boolean anonymize) {
        byte[] data = vishingDashboardService.exportReport(campaignId, format, anonymize);
        MediaType mediaType = "csv".equalsIgnoreCase(format)
                ? MediaType.parseMediaType("text/csv")
                : MediaType.APPLICATION_JSON;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vishing-report." + format)
                .contentType(mediaType)
                .body(data);
    }

    @Override
    public ResponseEntity<ApiResponseDto<VoiceLiveMetricsDto>> getLiveMetrics(String campaignId) {
        return ResponseEntity.ok(ApiResponseDto.<VoiceLiveMetricsDto>builder()
                .data(vishingDashboardService.getLiveMetrics(campaignId))
                .message("Live metrics retrieved successfully")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> sendTestCall(String campaignId, VoiceTestCallRequest request) {
        vishingDashboardService.sendTestCall(campaignId, request);
        return ResponseEntity.ok(ApiResponseDto.<String>builder()
                .data(request.getPhoneNumber())
                .message("Test call initiated")
                .build());
    }
}
