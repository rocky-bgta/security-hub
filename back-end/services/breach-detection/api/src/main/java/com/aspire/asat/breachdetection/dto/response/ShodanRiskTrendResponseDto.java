package com.aspire.asat.breachdetection.dto.response;

import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Powers the "Risk Trend — Last N Days" chart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShodanRiskTrendResponseDto {

    private ShodanSubjectType subjectType;
    private int days;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate from;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate to;

    @Builder.Default
    private List<ShodanRiskTrendBucketDto> buckets = new ArrayList<>();

    private long totalLow;
    private long totalMedium;
    private long totalHigh;
    private long totalCritical;
    private long total;
}
