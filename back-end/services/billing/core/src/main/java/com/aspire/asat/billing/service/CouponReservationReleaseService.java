package com.aspire.asat.billing.service;

public interface CouponReservationReleaseService {

    /**
     * Cancels unpaid invoices whose applied coupon has passed {@code couponValidUntil}
     * (status PENDING or ON_PROGRESS, no SUCCESS payment). Releases any reserved coupon usage
     * and deactivates related CLIENT licenses via registration.
     *
     * @return number of invoices cancelled
     */
    int cancelExpiredCouponInvoices();
}
