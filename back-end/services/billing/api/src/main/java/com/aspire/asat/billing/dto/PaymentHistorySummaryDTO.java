package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistorySummaryDTO {
    private Double totalPayments;              // Sum of all successful payments
    private Double totalPaymentsChange;      // Percentage change from last month
    private Double outstandingAmount;         // Sum of unpaid/partial payments
    private Double outstandingAmountChange;  // Percentage change from last month
    private Long paidInvoicesCount;          // Count of invoices with status PAID
    private Long totalInvoicesCount;         // Total invoices count
    private Long overdueInvoicesCount;       // Count of invoices with status OVERDUE
}

