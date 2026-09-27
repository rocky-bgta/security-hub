package com.aspire.asat.breachdetection.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated daily counts of email breach findings, broken down by severity.
 * Powers the "Breach Activity — Last N Days" chart on the dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreachActivityResponseDto {

    private int days;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate from;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate to;

    @Builder.Default
    private List<BreachActivityBucketDto> buckets = new ArrayList<>();

    private long totalCritical;
    private long totalHigh;
    private long totalMedium;
    private long totalLow;
    private long total;
}
