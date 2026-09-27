package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response DTO for trend data points.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataPointDto {

    private LocalDate date;
    private double value;
    private String label;
}
