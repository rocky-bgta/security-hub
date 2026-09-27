package com.aspire.asat.registration.data.invoice;

public enum InvoiceStatus {
    PENDING,    // Invoice created but not paid yet
    PAID,       // Fully paid
    PARTIAL,    // Partially paid
    OVERDUE,    // Due date passed and still unpaid
    CANCELLED,
    CREATED,
    EXPIRED     // Unpaid invoice past global expiresAt
}
