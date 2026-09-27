package com.aspire.asat.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Converts monetary amounts to US English words for invoice PDFs.
 */
public final class AmountInWordsConverter {

    private static final String[] ONES = {
            "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
            "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
            "seventeen", "eighteen", "nineteen"
    };

    private static final String[] TENS = {
            "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
    };

    private static final String[] SCALES = {"", " thousand", " million", " billion"};

    private AmountInWordsConverter() {
    }

    /**
     * Converts a USD amount to words, e.g. 955.94 -> "Nine hundred fifty-five dollars and ninety-four cents".
     */
    public static String toUsdAmountInWords(double amount) {
        BigDecimal normalized = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
        if (normalized.signum() < 0) {
            return capitalize("Zero dollars");
        }

        long dollars = normalized.longValue();
        int cents = normalized.remainder(BigDecimal.ONE)
                .movePointRight(2)
                .intValue();

        if (dollars == 0 && cents == 0) {
            return capitalize("Zero dollars");
        }

        if (dollars == 0) {
            return capitalize(formatCents(cents));
        }

        if (cents == 0) {
            return capitalize(formatDollars(dollars));
        }

        return capitalize(formatDollars(dollars) + " and " + formatCents(cents));
    }

    private static String formatDollars(long dollars) {
        String words = convertNumber(dollars);
        return words + (dollars == 1 ? " dollar" : " dollars");
    }

    private static String formatCents(int cents) {
        String words = convertNumber(cents);
        return words + (cents == 1 ? " cent" : " cents");
    }

    private static String convertNumber(long number) {
        if (number == 0) {
            return ONES[0];
        }

        StringBuilder result = new StringBuilder();
        int scaleIndex = 0;

        while (number > 0) {
            int chunk = (int) (number % 1000);
            if (chunk != 0) {
                String chunkWords = convertLessThanOneThousand(chunk);
                if (!result.isEmpty()) {
                    result.insert(0, " ");
                }
                result.insert(0, chunkWords + SCALES[scaleIndex]);
            }
            number /= 1000;
            scaleIndex++;
        }

        return result.toString().trim();
    }

    private static String convertLessThanOneThousand(int number) {
        StringBuilder result = new StringBuilder();

        if (number >= 100) {
            result.append(ONES[number / 100]).append(" hundred");
            number %= 100;
            if (number > 0) {
                result.append(" ");
            }
        }

        if (number >= 20) {
            result.append(TENS[number / 10]);
            if (number % 10 > 0) {
                result.append("-").append(ONES[number % 10]);
            }
        } else if (number > 0) {
            result.append(ONES[number]);
        }

        return result.toString();
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
