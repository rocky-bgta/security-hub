package com.aspire.asat.registration.data.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EnumValueValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEnum {
    Class<? extends Enum<?>> enumClass();
    String message() default "must be one of the allowed values";
    boolean required() default true;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
