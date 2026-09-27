package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.SuccessKeywordsRequest;
import com.aspire.asat.phishing.dto.request.TeachableMomentRequest;
import com.aspire.asat.phishing.dto.request.VoiceTestCallRequest;
import com.aspire.asat.phishing.dto.response.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Vishing Dashboard", description = "Dashboard APIs for vishing campaigns")
@RequestMapping(value = WebApiUrlConstants.CAMPAIGNS_PATH)
public interface VishingDashboardController {

    @Operation(summary = "Get vishing data capture logs")
    @GetMapping("/{campaignId}/vishing/data-capture")
    ResponseEntity<ApiResponseDto<VishingDataCaptureDto>> getDataCapture(
            @PathVariable String campaignId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int pageSize);

    @Operation(summary = "Get success keywords")
    @GetMapping("/{campaignId}/vishing/success-keywords")
    ResponseEntity<ApiResponseDto<List<String>>> getSuccessKeywords(@PathVariable String campaignId);

    @Operation(summary = "Update success keywords")
    @PutMapping("/{campaignId}/vishing/success-keywords")
    ResponseEntity<ApiResponseDto<List<String>>> updateSuccessKeywords(
            @PathVariable String campaignId,
            @Valid @RequestBody SuccessKeywordsRequest request);

    @Operation(summary = "Get remediation metrics")
    @GetMapping("/{campaignId}/vishing/remediation")
    ResponseEntity<ApiResponseDto<VishingRemediationDto>> getRemediation(@PathVariable String campaignId);

    @Operation(summary = "Send teachable moment")
    @PostMapping("/{campaignId}/vishing/teachable-moment")
    ResponseEntity<ApiResponseDto<String>> sendTeachableMoment(
            @PathVariable String campaignId,
            @Valid @RequestBody TeachableMomentRequest request);

    @Operation(summary = "Get vishing report")
    @GetMapping("/{campaignId}/vishing/report")
    ResponseEntity<ApiResponseDto<VishingReportDto>> getReport(@PathVariable String campaignId);

    @Operation(summary = "Export vishing report")
    @GetMapping("/{campaignId}/vishing/report/export")
    ResponseEntity<byte[]> exportReport(
            @PathVariable String campaignId,
            @RequestParam(defaultValue = "json") String format,
            @RequestParam(defaultValue = "false") boolean anonymize);

    @Operation(summary = "Get live voice metrics")
    @GetMapping("/{campaignId}/vishing/live-metrics")
    ResponseEntity<ApiResponseDto<VoiceLiveMetricsDto>> getLiveMetrics(@PathVariable String campaignId);

    @Operation(summary = "Send test call")
    @PostMapping("/{campaignId}/vishing/test-call")
    ResponseEntity<ApiResponseDto<String>> sendTestCall(
            @PathVariable String campaignId,
            @Valid @RequestBody VoiceTestCallRequest request);
}
