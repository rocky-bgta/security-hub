package com.aspire.asat.billing.repo.customRepo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummaryResult {
    private Double totalPayments;
    private Double outstandingAmount;
    private Long paidInvoicesCount;
    private Long totalInvoicesCount;
    private Long overdueInvoicesCount;
}

