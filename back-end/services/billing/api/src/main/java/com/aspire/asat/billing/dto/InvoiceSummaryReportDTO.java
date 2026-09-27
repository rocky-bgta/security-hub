package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceSummaryReportDTO {
    private Long totalInvoiceCount;
    private Long paidInvoiceCount;
    private Long unpaidInvoiceCount;
    private Long cancelledInvoiceCount;
    private Long onProgressInvoiceCount;
}
