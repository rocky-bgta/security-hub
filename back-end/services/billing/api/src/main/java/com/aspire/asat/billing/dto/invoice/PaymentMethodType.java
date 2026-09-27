package com.aspire.asat.billing.dto.invoice;

/**
 * Enum representing the payment method used for completed payments.
 */
public enum PaymentMethodType {
    BANK_TRANSFER,  // Bank transfer / Direct transfer
    CHECK_PAYMENT,  // Check payment / Paper check
    STRIPE,         // Online payment via Stripe
    PAYPAL          // Online payment via PayPal
}

