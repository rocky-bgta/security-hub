package com.aspire.asat.billing.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    String message() default "validUntil must be after validFrom";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
