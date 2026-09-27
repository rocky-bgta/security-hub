package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSummaryReportDTO {
    private Long totalPaymentCount;
    private Long successPaymentCount;
    private Long pendingPaymentCount;
    private Long failedPaymentCount;
    private Long cancelledPaymentCount;
    private Double totalSuccessAmount;
    private Double totalPendingAmount;
    private Double totalFailedAmount;
    private Double totalCancelledAmount;
}
