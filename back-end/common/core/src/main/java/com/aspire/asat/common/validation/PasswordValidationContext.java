package com.aspire.asat.common.validation;

import lombok.Builder;
import lombok.Data;

/**
 * Optional context for password validation to reject personal information.
 * Pass user email, name parts so the validator can reject passwords containing them.
 */
@Data
@Builder
public class PasswordValidationContext {
    private String email;
    private String firstName;
    private String lastName;
    private String username;

    public static PasswordValidationContext empty() {
        return PasswordValidationContext.builder().build();
    }
}
