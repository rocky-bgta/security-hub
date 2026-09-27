package com.aspire.asat.phishing.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Embedded model for recurring campaign configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringConfig {

    private String frequency;           // DAILY, WEEKLY, MONTHLY

    private List<Integer> daysOfWeek;   // For weekly: 1=Monday, 7=Sunday

    private Integer dayOfMonth;         // For monthly: 1-31

    private String timeOfDay;           // HH:mm format

    private Integer repeatCount;        // Number of repetitions (null = unlimited)

    private boolean neverExpires;
}
