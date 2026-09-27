package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.AnalyticsController;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.response.UserRiskSummaryDto;
import com.aspire.asat.phishing.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller implementation for analytics endpoints.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class AnalyticsControllerImpl implements AnalyticsController {

    private final AnalyticsService analyticsService;

    @Override
    public ResponseEntity<ApiResponseDto<Double>> getPhishPronePercentage() {
        try {
            double percentage = analyticsService.getPhishPronePercentage();
            return ResponseEntity.ok(ApiResponseDto.<Double>builder()
                    .data(percentage)
                    .message("Phish-prone percentage calculated")
                    .build());
        } catch (Exception e) {
            log.error("Error calculating phish-prone percentage", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<Double>builder()
                            .message("Failed to calculate: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Double>> getPhishPronePercentageForCampaign(String campaignId) {
        try {
            double percentage = analyticsService.getPhishPronePercentageForCampaign(campaignId);
            return ResponseEntity.ok(ApiResponseDto.<Double>builder()
                    .data(percentage)
                    .message("Phish-prone percentage calculated for campaign")
                    .build());
        } catch (Exception e) {
            log.error("Error calculating phish-prone percentage for campaign", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<Double>builder()
                            .message("Failed to calculate: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<UserRiskSummaryDto>>> getRepeatOffenders(int limit) {
        try {
            List<UserRiskSummaryDto> offenders = analyticsService.getRepeatOffenders(limit);
            return ResponseEntity.ok(ApiResponseDto.<List<UserRiskSummaryDto>>builder()
                    .data(offenders)
                    .message("Repeat offenders retrieved")
                    .build());
        } catch (Exception e) {
            log.error("Error getting repeat offenders", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<List<UserRiskSummaryDto>>builder()
                            .message("Failed to retrieve: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Double>> getDeliveryRate() {
        try {
            double rate = analyticsService.getDeliveryRate();
            return ResponseEntity.ok(ApiResponseDto.<Double>builder()
                    .data(rate)
                    .message("Delivery rate calculated")
                    .build());
        } catch (Exception e) {
            log.error("Error calculating delivery rate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<Double>builder()
                            .message("Failed to calculate: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Double>> getCompromiseRate() {
        try {
            double rate = analyticsService.getCompromiseRate();
            return ResponseEntity.ok(ApiResponseDto.<Double>builder()
                    .data(rate)
                    .message("Compromise rate calculated")
                    .build());
        } catch (Exception e) {
            log.error("Error calculating compromise rate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<Double>builder()
                            .message("Failed to calculate: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Double>> getReportRate() {
        try {
            double rate = analyticsService.getReportRate();
            return ResponseEntity.ok(ApiResponseDto.<Double>builder()
                    .data(rate)
                    .message("Report rate calculated")
                    .build());
        } catch (Exception e) {
            log.error("Error calculating report rate", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<Double>builder()
                            .message("Failed to calculate: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<UserRiskSummaryDto>> getUserRiskSummary(String userId) {
        try {
            UserRiskSummaryDto summary = analyticsService.getUserRiskSummary(userId);
            if (summary == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponseDto.<UserRiskSummaryDto>builder()
                                .message("User risk profile not found")
                                .build());
            }
            return ResponseEntity.ok(ApiResponseDto.<UserRiskSummaryDto>builder()
                    .data(summary)
                    .message("User risk summary retrieved")
                    .build());
        } catch (Exception e) {
            log.error("Error getting user risk summary", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<UserRiskSummaryDto>builder()
                            .message("Failed to retrieve: " + e.getMessage())
                            .build());
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> recalculateRiskProfiles() {
        try {
            analyticsService.recalculateUserRiskProfiles();
            return ResponseEntity.ok(ApiResponseDto.<String>builder()
                    .data("success")
                    .message("User risk profiles recalculated successfully")
                    .build());
        } catch (Exception e) {
            log.error("Error recalculating risk profiles", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.<String>builder()
                            .message("Failed to recalculate: " + e.getMessage())
                            .build());
        }
    }
}
