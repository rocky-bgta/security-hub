package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for 7 core KPI cards (from BRD Use Case 2.1.0.1).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardKpiDto {

    private int attacks;       // Total phishing attacks launched
    private int hacks;         // Number of successful compromises
    private int reports;       // Number of phishing emails reported
    private int campaigns;     // Total phishing campaigns created
    private int templates;     // Total available email templates
    private int groups;        // Number of recipient groups created
    private int landingPages;  // Number of phishing landing pages available

    // Additional derived metrics
    private double compromiseRate;  // hacks / attacks * 100
    private double reportRate;      // reports / attacks * 100
}
