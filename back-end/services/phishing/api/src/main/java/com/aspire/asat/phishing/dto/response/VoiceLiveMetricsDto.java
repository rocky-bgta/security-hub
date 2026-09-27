package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceLiveMetricsDto {

    private int activeCalls;
    private int answeredCalls;
    private int engagedCalls;
    private int completedCalls;
    private Double averageSttLatencyMs;
    private Double averageLlmLatencyMs;
    private Double averageTtsLatencyMs;
}
