package com.aspire.asat.breachdetection.dto.response;

import com.aspire.asat.breachdetection.dto.enums.ImpersonationTactic;
import com.aspire.asat.breachdetection.dto.enums.ShodanSubjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Backs the "Tactic Distribution" pie chart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShodanTacticDistributionDto {

    private ShodanSubjectType subjectType;
    private long total;

    @Builder.Default
    private List<Slice> slices = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Slice {
        private ImpersonationTactic tactic;
        private long count;
        /** 0..100, rounded to 2 decimals. */
        private double percentage;
    }
}
