package com.aspire.asat.billing.dto.invoice;

/**
 * Enum representing the reason for payment failure.
 * Used for analytics and reporting purposes.
 */
public enum PaymentFailureReason {
    INSUFFICIENT_FUNDS("Insufficient Funds"),
    EXPIRED_CARD("Expired Card"),
    DECLINED_BY_BANK("Declined by Bank"),
    INVALID_CARD("Invalid Card"),
    FRAUD_SUSPECTED("Fraud Suspected"),
    OTHER("Other");

    private final String displayName;

    PaymentFailureReason(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

