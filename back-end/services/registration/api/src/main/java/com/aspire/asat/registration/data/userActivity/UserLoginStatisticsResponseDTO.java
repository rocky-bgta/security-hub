package com.aspire.asat.registration.data.userActivity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response DTO for User Login Statistics report data
 * Supports different time periods: daily (7Day), weekly (1Month), monthly (12Month)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginStatisticsResponseDTO {

    /**
     * The date for which the login count is reported
     * For daily: specific date, For weekly: start of week, For monthly: first day of month
     */
    private LocalDate date;

    /**
     * Number of login and logout activities combined for this period
     */
    private Long loginCount;

    /**
     * Formatted date string for display purposes 
     * Daily: "2024-01-15", Weekly: "2024-W03", Monthly: "2024-01"
     */
    private String dateString;

    /**
     * Period name for display (e.g., "January" for monthly, "Week 3" for weekly, "January" for daily)
     */
    private String monthName;

    /**
     * Year for the data point
     */
    private Integer year;

    /**
     * Month number (1-12) for sorting
     */
    private Integer month;
}
