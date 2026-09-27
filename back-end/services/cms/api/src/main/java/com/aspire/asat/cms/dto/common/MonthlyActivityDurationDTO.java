package com.aspire.asat.cms.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class MonthlyActivityDurationDTO {
    private long jan;
    private long feb;
    private long mar;
    private long apr;
    private long may;
    private long jun;
    private long jul;
    private long aug;
    private long sep;
    private long oct;
    private long nov;
    private long dec;
}
