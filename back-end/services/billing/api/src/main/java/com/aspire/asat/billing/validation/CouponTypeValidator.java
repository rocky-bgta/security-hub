package com.aspire.asat.billing.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class CouponTypeValidator implements ConstraintValidator<ValidCouponType, String> {

    private static final Set<String> ALLOWED_TYPES = Set.of("FIXED", "PERCENTAGE");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && ALLOWED_TYPES.contains(value.toUpperCase());
    }
}
