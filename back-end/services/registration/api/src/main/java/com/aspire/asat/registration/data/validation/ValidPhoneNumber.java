package com.aspire.asat.registration.data.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for validating phone numbers based on country.
 * This annotation validates that the phone number format matches the expected
 * pattern for the specified country.
 */
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {
    String message() default "Phone number format is invalid for the specified country";
    
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    /**
     * The field name that contains the phone number
     */
    String phoneNumberField() default "phoneNumber";
    
    /**
     * The field name that contains the country (e.g. country ID)
     */
    String countryField() default "country";

    /**
     * Optional field name that contains the country name for error messages.
     * When set and non-empty, the validation error will show this instead of the country field value.
     */
    String countryNameField() default "";
}
