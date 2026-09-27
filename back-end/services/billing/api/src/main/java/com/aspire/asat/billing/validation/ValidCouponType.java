package com.aspire.asat.billing.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CouponTypeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCouponType {
    String message() default "Coupon type must be either FIXED or PERCENTAGE";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
