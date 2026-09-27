package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VishingReportDto {

    private String campaignId;
    private String campaignName;
    private int totalRecipients;
    private int callsTotal;
    private int callsAnswered;
    private int callsEngaged;
    private int callsReported;
    private int callsCompromised;
    private int callsNoAnswer;
    private int callsFailed;
    private int retriesTriggered;
    private Double averageSttLatencyMs;
    private Double averageLlmLatencyMs;
    private Double averageTtsLatencyMs;
    private boolean consentConfirmed;
    private String learningMode;
}
