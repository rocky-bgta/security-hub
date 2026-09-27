package com.aspire.asat.cms.dto.subPackage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Represents reminder dates for a subpackage
 */

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReminderDates {
    private LocalDate reminderNotStarted;
    private LocalDate reminder50Percent;
    private LocalDate reminder20Percent;
    private LocalDate reminder10Percent;
    private LocalDate expiryDate;
}