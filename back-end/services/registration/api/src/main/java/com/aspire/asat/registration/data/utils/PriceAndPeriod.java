package com.aspire.asat.registration.data.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Helper class to hold price and period for product total calculation.
 * Used for determining which price (yearly or monthly) and period to use
 * based on validity unit when calculating product totals.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceAndPeriod {
    private double price;
    private int period;
}

