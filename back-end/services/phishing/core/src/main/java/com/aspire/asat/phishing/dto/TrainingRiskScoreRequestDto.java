package com.aspire.asat.phishing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for updating training risk score: clientAdminId and riskScore.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRiskScoreRequestDto {

    private String clientAdminId;
    private Double riskScore;
}
