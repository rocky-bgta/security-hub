package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for breach summary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreachSummaryDto {

    private int totalBreaches;
    private int affectedUsers;
    private int uniqueDomains;
    private int resolvedBreaches;
    private int pendingActions;

    private int breachesThisMonth;
    private int breachesLastMonth;
    private double monthOverMonthChange;

    @Builder.Default
    private List<BreachBySourceDto> breachesBySource = new ArrayList<>();
}
