package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for bundled dashboard trend series.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardTrendsDto {

    @Builder.Default
    private List<TrendDataPointDto> openRateTrend = new ArrayList<>();

    @Builder.Default
    private List<TrendDataPointDto> clickRateTrend = new ArrayList<>();

    @Builder.Default
    private List<TrendDataPointDto> submissionRateTrend = new ArrayList<>();

    @Builder.Default
    private List<TrendDataPointDto> reportRateTrend = new ArrayList<>();
}
