package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditUsageSummaryDTO {
    private double totalUsedCredit;
    private double totalPaidCredit;
    private double totalDueCredit;
}

