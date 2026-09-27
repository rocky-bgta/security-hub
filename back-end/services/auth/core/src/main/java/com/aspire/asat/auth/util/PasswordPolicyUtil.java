package com.aspire.asat.auth.util;

import com.aspire.asat.common.validation.PasswordValidationContext;
import com.aspire.asat.common.validation.PasswordValidationResult;
import com.aspire.asat.common.validation.PasswordValidator;

/**
 * Delegates to common {@link PasswordValidator}. Use for backward compatibility.
 * Prefer {@link PasswordValidator#validate(String, PasswordValidationContext)} where context is available.
 */
public final class PasswordPolicyUtil {

    private PasswordPolicyUtil() {
    }

    public static boolean isValid(String password) {
        return PasswordValidator.isValid(password);
    }

    public static String getPolicyMessage() {
        return PasswordValidator.getPolicyMessage();
    }

    /**
     * Validate and return full result (errors list).
     */
    public static PasswordValidationResult validate(String password) {
        return PasswordValidator.validate(password);
    }

    /**
     * Validate with user context (email, name) to reject personal info.
     */
    public static PasswordValidationResult validate(String password, PasswordValidationContext context) {
        return PasswordValidator.validate(password, context);
    }
}
