package com.aspire.asat.registration.data.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

/**
 * Database-driven validator implementation for country-wise phone number validation.
 * This validator is designed to work with country phone codes stored in the database.
 * Currently uses a generic international pattern as fallback.
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, Object> {

    private String phoneNumberField;
    private String countryField;
    private String countryNameField;
    
    // Generic international pattern for all countries
    private static final Pattern INTERNATIONAL_PATTERN = Pattern.compile("^\\+\\d{1,4}\\d{7,15}$");

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        this.phoneNumberField = constraintAnnotation.phoneNumberField();
        this.countryField = constraintAnnotation.countryField();
        this.countryNameField = constraintAnnotation.countryNameField();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }

        try {
            String phoneNumber = getFieldValue(value, phoneNumberField);
            String country = getFieldValue(value, countryField);

            if (phoneNumber == null || country == null) {
                return true; // Let other validations handle null values
            }

            // Validate phone number format using generic international pattern
            boolean isValid = INTERNATIONAL_PATTERN.matcher(phoneNumber.trim()).matches();
            
            if (!isValid) {
                String countryDisplay = getCountryDisplayName(value, country);
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    String.format("Phone number '%s' format is invalid for country '%s'. Expected format: +[country code][7-15 digits] (e.g., +1234567890)", 
                        phoneNumber, countryDisplay)
                ).addConstraintViolation();
            }
            
            return isValid;

        } catch (Exception e) {
            // If there's an error accessing fields, consider it invalid
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Error validating phone number: " + e.getMessage()
            ).addConstraintViolation();
            return false;
        }
    }

    /**
     * Returns the country name for display in error messages when countryNameField is set and non-empty;
     * otherwise returns the country field value (e.g. country ID).
     */
    private String getCountryDisplayName(Object value, String countryFallback) {
        if (countryNameField == null || countryNameField.isBlank()) {
            return countryFallback;
        }
        try {
            String name = getFieldValue(value, countryNameField);
            return (name != null && !name.isBlank()) ? name.trim() : countryFallback;
        } catch (Exception e) {
            return countryFallback;
        }
    }

    private String getFieldValue(Object object, String fieldName) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        Object value = field.get(object);
        return value != null ? value.toString() : null;
    }
}