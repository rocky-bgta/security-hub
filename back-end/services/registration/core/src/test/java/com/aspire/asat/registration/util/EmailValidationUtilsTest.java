package com.aspire.asat.registration.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmailValidationUtilsTest {

    @Test
    void isValidEmail_ValidAndInvalidCases() {
        assertTrue(EmailValidationUtils.isValidEmail("user@company.com"));
        assertTrue(EmailValidationUtils.isValidEmail("  user.name+tag@company.co.uk  "));
        assertFalse(EmailValidationUtils.isValidEmail(null));
        assertFalse(EmailValidationUtils.isValidEmail(""));
        assertFalse(EmailValidationUtils.isValidEmail("not-an-email"));
        assertFalse(EmailValidationUtils.isValidEmail("user@"));
        assertFalse(EmailValidationUtils.isValidEmail("@company.com"));
    }
}
