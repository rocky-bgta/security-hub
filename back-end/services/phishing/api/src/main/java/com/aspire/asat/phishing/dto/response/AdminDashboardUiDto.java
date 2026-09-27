package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.CohortRiskTrendUi;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-admin security risk dashboard payload (widgets + optional table rows).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardUiDto {

    private HumanRiskScoreUiDto humanRiskScore;

    @Builder.Default
    private List<PhishProneTierUiDto> phishProneUsers = new ArrayList<>();

    private int informationSubmits;

    private ReportRateUiDto reportRate;

    /** Overall cohort risk trend vs previous period (JSON: improving / worsening / stable). */
    private CohortRiskTrendUi riskTrend;

    /**
     * Optional: top risky users for a minimal table (omitted from JSON when empty).
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Builder.Default
    private List<TopRiskUserDto> topRiskUsers = new ArrayList<>();
}
