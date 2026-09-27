package com.aspire.asat.cms.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for Phishing service updateTrainingRiskScore endpoint.
 * Matches TrainingRiskScoreRequestDto (clientAdminId, riskScore).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhishingTrainingRiskScoreRequestDto {

    private String clientAdminId;
    private Double riskScore;
}
