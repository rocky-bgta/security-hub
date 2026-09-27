package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopRiskUserDto {
    private String userId;
    private String email;
    private double phishingRiskScore;
    private RiskLevel riskLevel;
}
