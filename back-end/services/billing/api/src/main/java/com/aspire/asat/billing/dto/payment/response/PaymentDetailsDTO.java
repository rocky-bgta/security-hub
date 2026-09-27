package com.aspire.asat.billing.dto.payment.response;

import com.aspire.asat.billing.dto.DiscountedItemDTO;
import com.aspire.asat.billing.dto.PaymentSourceDTO;
import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.dto.invoice.ProductSelectionDto;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class PaymentDetailsDTO {

    // From Payment
    private String paymentId;
    private String invoiceId;
    private Double amount;
    private String currency;
    private String status;
    private boolean online;
    private boolean isActive;
    private String clientId;
    private String notes;
    private Instant paymentDate;
    private Instant createdAt;
    private String couponId;
    private String couponCode;
    private Double discountAmount;
    private Double actualAmount;
    private Double subtotal;
    private Double vatAmount;
    private String transactionId;
    private Object metaData;
    private List<DiscountedItemDTO> breakdown;
    private List<PaymentSourceDTO> paymentSources;
    private String invoiceFileKey;
    private boolean receiptGenerated;

    // From Invoice
    private String clientAdminId;
    private String clientName;
    private List<String> clientProductIds;
    private double invoiceSubtotal;
    private double invoiceDiscountAmount;
    private double invoiceVatAmount;
    private double totalAmount;
    private String statusNote;
    private InvoiceStatus invoiceStatus;
    private String invoicePdfLink;
    private Instant invoiceCreatedAt;
    private Instant paidAt;
    private List<ProductSelectionDto> productSelections;
}
