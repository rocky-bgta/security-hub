package com.aspire.asat.billing.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CurrencyRequiredIfFixedValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrencyRequiredIfFixed {
    String message() default "Currency is required when coupon type is FIXED";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
