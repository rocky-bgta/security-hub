package com.aspire.asat.billing.dto;

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
public class PaymentHistoryItemDTO {
    private String id;              // MongoDB payment ID
    private String invoiceId;
    private String invoiceNumber;
    private String date;  // Payment date
    private Double amount;
    private Double dueAmount;
    private String status;         // e.g., PAID, PENDING, FAILED
    private String paymentMethod;  // e.g., Credit Card, Bank Transfer
    private String clientId;       // From Payment (same as clientAdminId)
    private String clientName;     // From Invoice
    private String mspId;          // From Payment.mspAdminId (same as mspAdminId)
    private String mspName;        // From Invoice
    private String countryId;       // From Payment
    private String countryName;     // From Invoice
    private RoleType roleType;      // From Payment (CLIENT or MSP)
    private Instant invoiceDate;    // From Invoice.createdAt
    private Double totalAmount;    // From Invoice.totalAmount
    private Double outstanding;    // Calculated as totalAmount - amount
    private Double discountAmount;  // Invoice-level discount
    private String discountType;    // FLAT or PERCENTAGE from Invoice
    private Double discountPercentage; // Invoice-level discount percentage
    private String couponCode;      // From Invoice (fallback Payment)
    private Double couponDiscountAmount; // Coupon discount from invoice
    private Double actualAmount;    // From Payment.actualAmount
    private Double subtotal;        // From Invoice.subtotal
    private Double vatAmount;       // From Invoice.vatAmount
    private String currency;        // From Payment
    private String transactionId;   // From Payment
    private String paymentType;     // From Payment.paymentType
}
