package com.aspire.asat.billing.dto;

import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.dto.invoice.PaymentMethodType;
import com.aspire.asat.billing.dto.invoice.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceHistoryItemDTO {
    private String id;              // Invoice ID
    private String invoiceNumber;  // Invoice number (same as id or formatted)
    private String clientId;        // Client Admin ID
    private String clientName;      // Client organization name
    private String mspId;          // MSP Admin ID
    private String mspName;        // MSP organization name
    private String countryId;       // Country ID
    private String countryName;     // Country name
    private Instant invoiceDate;    // Invoice creation date (createdAt)
    private Instant paidDate;       // Invoice paid date (paidAt)
    private InvoiceStatus status;   // Invoice status
    private RoleType roleType;      // CLIENT or MSP
    private Double subtotal;        // Subtotal before discounts and VAT
    private Double discountAmount;    // Discount amount applied
    private Double couponDiscountAmount; // Coupon discount amount applied
    private Double vatAmount;       // VAT amount
    private Double totalAmount;    // Total amount after discount and VAT
    private Double outstandingAmount; // Outstanding amount (calculated)
    private PaymentMethodType paymentMethod; // Payment method if paid
}

