package com.aspire.asat.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsedCreditPaymentRequestDTO {

    private String creditTransactionId;  // Required: Which credit transaction you're paying for
    private String clientId;             // Required
    private String invoiceId;            // Optional
    private String method;               // STRIPE, PAYPAL, MANUAL
    private double amount;               // Payment amount
    private String referenceId;          // Stripe session ID, PayPal ID, or bank reference (optional)
    private String remarks;              // Notes
    private String bankName;             // Only for manual
    private String bankAccount;          // Only for manual
    private String paidBy;               // Optional: Who made the payment
}
