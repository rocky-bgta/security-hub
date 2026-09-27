package com.aspire.asat.common.enums;

public enum InvoiceStatus {
    ON_PROGRESS,
    PENDING,    // Invoice created but not paid yet
    PAID,       // Fully paid
    PARTIAL,    // Partially paid
    OVERDUE,    // Due date passed and still unpaid
    CANCELLED,  // Invoice was cancelled (e.g. coupon expiry)
    EXPIRED     // Unpaid invoice past global expiresAt
}

