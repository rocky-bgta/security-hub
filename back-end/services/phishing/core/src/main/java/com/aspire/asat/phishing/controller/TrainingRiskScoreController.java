package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.TrainingRiskScoreRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for updating UserRiskProfile.trainingRiskScore from a single riskScore value.
 */
@Tag(name = "Training Risk Score", description = "Update user training risk score")
@RequestMapping(value = WebApiUrlConstants.TRAINING_RISK_SCORE_PATH, produces = "application/json")
public interface TrainingRiskScoreController {

    @Operation(summary = "Update training risk score by user", description = "POST body with clientAdminId and riskScore. The riskScore is stored in UserRiskProfile.trainingRiskScore; overall riskScore is updated as weighted (30% training, 70% phishing) when phishing score exists.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Training risk score updated"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{userId}")
    ResponseEntity<ApiResponseDto<Double>> updateTrainingRiskScore(
            @Parameter(description = "User ID") @PathVariable String userId,
            @RequestBody TrainingRiskScoreRequestDto body);
}
