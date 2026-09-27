package com.aspire.asat.billing.service;

public interface InvoiceExpiryService {

    /**
     * Marks unpaid PENDING/ON_PROGRESS invoices past {@code expiresAt} as EXPIRED.
     * Releases reserved coupons and cascades CLIENT license expiry via registration.
     *
     * @return number of invoices marked EXPIRED
     */
    int expireOverdueInvoices();
}
