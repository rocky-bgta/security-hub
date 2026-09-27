package com.aspire.asat.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Normalizes monetary values to 2 decimal places (HALF_UP) for invoice and payment calculations.
 */
public final class MoneyUtil {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private MoneyUtil() {
    }

    public static double round(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0;
        }
        return BigDecimal.valueOf(value).setScale(SCALE, ROUNDING).doubleValue();
    }

    public static Double round(Double value) {
        if (value == null) {
            return null;
        }
        return round(value.doubleValue());
    }

    public static long toCents(double amount) {
        return BigDecimal.valueOf(round(amount))
                .movePointRight(2)
                .setScale(0, ROUNDING)
                .longValueExact();
    }

    public static boolean isEqual(double a, double b) {
        return round(a) == round(b);
    }
}
