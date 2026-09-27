package com.aspire.asat.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoneyUtilTest {

    @Test
    void round_halfUpToTwoDecimals() {
        assertEquals(100.35, MoneyUtil.round(100.346));
        assertEquals(100.34, MoneyUtil.round(100.344));
    }

    @Test
    void toCents_usesHalfUp() {
        assertEquals(10035L, MoneyUtil.toCents(100.346));
        assertEquals(10034L, MoneyUtil.toCents(100.344));
    }

    @Test
    void isEqual_comparesRoundedValues() {
        assertTrue(MoneyUtil.isEqual(100.346, 100.35));
    }

    @Test
    void invoiceStyleCalculation_matchesAtTwoDecimals() {
        double subtotal = 1000.0;
        double couponDiscount = MoneyUtil.round(subtotal * 0.10);
        double subtotalAfterCoupon = MoneyUtil.round(subtotal - couponDiscount);
        double invoiceDiscount = MoneyUtil.round(subtotalAfterCoupon * 0.05);
        double discountedSubtotal = MoneyUtil.round(subtotalAfterCoupon - invoiceDiscount);
        double vat = MoneyUtil.round(discountedSubtotal * 0.15);
        double invoiceTotal = MoneyUtil.round(discountedSubtotal + vat);
        double paymentAmount = MoneyUtil.round(invoiceTotal);

        assertEquals(983.25, invoiceTotal);
        assertEquals(invoiceTotal, paymentAmount);
    }
}
