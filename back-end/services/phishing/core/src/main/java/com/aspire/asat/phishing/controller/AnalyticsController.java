package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.response.UserRiskSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller interface for analytics endpoints.
 */
@Tag(name = "Analytics", description = "APIs for analytics calculations")
@RequestMapping(value = WebApiUrlConstants.ANALYTICS_PATH)
public interface AnalyticsController {

    @Operation(summary = "Get phish-prone percentage", 
               description = "Calculate phish-prone % = (clicked / sent) * 100")
    @GetMapping("/phish-prone-percentage")
    ResponseEntity<ApiResponseDto<Double>> getPhishPronePercentage();

    @Operation(summary = "Get phish-prone percentage for campaign")
    @GetMapping("/phish-prone-percentage/{campaignId}")
    ResponseEntity<ApiResponseDto<Double>> getPhishPronePercentageForCampaign(
            @PathVariable String campaignId
    );

    @Operation(summary = "Get repeat offenders", 
               description = "List users who clicked 3+ times")
    @GetMapping("/repeat-offenders")
    ResponseEntity<ApiResponseDto<List<UserRiskSummaryDto>>> getRepeatOffenders(
            @Parameter(description = "Maximum number of results")
            @RequestParam(defaultValue = "20") int limit
    );

    @Operation(summary = "Get delivery rate", 
               description = "Email delivery success rate")
    @GetMapping("/delivery-rate")
    ResponseEntity<ApiResponseDto<Double>> getDeliveryRate();

    @Operation(summary = "Get compromise rate", 
               description = "User compromise rate (data submitted / clicked)")
    @GetMapping("/compromise-rate")
    ResponseEntity<ApiResponseDto<Double>> getCompromiseRate();

    @Operation(summary = "Get report rate", 
               description = "Phishing report rate by users")
    @GetMapping("/report-rate")
    ResponseEntity<ApiResponseDto<Double>> getReportRate();

    @Operation(summary = "Get user risk summary")
    @GetMapping("/user-risk/{userId}")
    ResponseEntity<ApiResponseDto<UserRiskSummaryDto>> getUserRiskSummary(
            @PathVariable String userId
    );

    @Operation(summary = "Recalculate user risk profiles")
    @PostMapping("/recalculate-risk")
    ResponseEntity<ApiResponseDto<String>> recalculateRiskProfiles();
}
