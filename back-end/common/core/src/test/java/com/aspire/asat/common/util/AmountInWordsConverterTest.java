package com.aspire.asat.common.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AmountInWordsConverterTest {

    @ParameterizedTest
    @CsvSource({
            "955.94, 'Nine hundred fifty-five dollars and ninety-four cents'",
            "955.00, 'Nine hundred fifty-five dollars'",
            "0.94, 'Ninety-four cents'",
            "0.00, 'Zero dollars'",
            "1.00, 'One dollar'",
            "2.01, 'Two dollars and one cent'",
            "1000000.01, 'One million dollars and one cent'",
            "955.939999, 'Nine hundred fifty-five dollars and ninety-four cents'"
    })
    void toUsdAmountInWords_convertsExpectedValues(double amount, String expected) {
        assertEquals(expected, AmountInWordsConverter.toUsdAmountInWords(amount));
    }

    @ParameterizedTest
    @CsvSource({
            "-10.00, 'Zero dollars'",
            "-0.01, 'Zero dollars'"
    })
    void toUsdAmountInWords_negativeAmountsReturnZeroDollars(double amount, String expected) {
        assertEquals(expected, AmountInWordsConverter.toUsdAmountInWords(amount));
    }
}
