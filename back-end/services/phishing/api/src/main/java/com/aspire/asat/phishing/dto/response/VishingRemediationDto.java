package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VishingRemediationDto {

    private double answerRate;
    private double engagedRate;
    private double compromiseRate;
    private double failureRate;
    private double averageRiskScore;
    private List<String> failedRecipientIds;
    private int trainingAssignedCount;
    private int trainingCompletedCount;
}
