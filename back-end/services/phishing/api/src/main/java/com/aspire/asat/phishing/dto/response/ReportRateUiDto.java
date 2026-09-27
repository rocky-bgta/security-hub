package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.ReportRateTrendUi;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Report rate widget with formatted current value and micro-trend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRateUiDto {
    /** e.g. "18.0%" */
    private String current;
    private ReportRateTrendUi trend;
}
