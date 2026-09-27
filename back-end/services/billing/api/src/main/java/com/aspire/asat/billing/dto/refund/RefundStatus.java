package com.aspire.asat.billing.dto.refund;

/**
 * Enum representing the status of a refund.
 */
public enum RefundStatus {
    PENDING,    // Refund initiated but not yet processed
    PROCESSED,  // Refund successfully processed
    FAILED      // Refund processing failed
}

