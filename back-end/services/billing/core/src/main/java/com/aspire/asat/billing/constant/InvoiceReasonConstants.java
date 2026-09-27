package com.aspire.asat.billing.constant;

public final class InvoiceReasonConstants {

    private InvoiceReasonConstants() {}

    public static final String EXPIRED_UNPAID =
            "Payment not received within the due date";

    public static final String CANCELLED_COUPON_EXPIRED =
            "Invoice cancelled because the applied coupon expired before payment";
}
