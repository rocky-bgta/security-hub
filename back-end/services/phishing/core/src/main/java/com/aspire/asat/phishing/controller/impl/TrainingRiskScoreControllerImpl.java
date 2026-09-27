package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.TrainingRiskScoreController;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.TrainingRiskScoreRequestDto;
import com.aspire.asat.phishing.service.TrainingRiskScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TrainingRiskScoreControllerImpl implements TrainingRiskScoreController {

    private final TrainingRiskScoreService trainingRiskScoreService;

    @Override
    public ResponseEntity<ApiResponseDto<Double>> updateTrainingRiskScore(String userId, TrainingRiskScoreRequestDto body) {
        if (userId == null || userId.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>("userId is required", 400, null));
        }
        if (body == null || body.getClientAdminId() == null || body.getClientAdminId().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>("clientAdminId is required", 400, null));
        }
        try {
            Double riskScore = trainingRiskScoreService.updateTrainingRiskScore(userId, body.getClientAdminId(), body.getRiskScore());
            return ResponseEntity.ok(new ApiResponseDto<>("Training risk score updated", 200, riskScore));
        } catch (Exception e) {
            log.error("Error updating training risk score for userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to update: " + e.getMessage(), 500, null));
        }
    }
}
