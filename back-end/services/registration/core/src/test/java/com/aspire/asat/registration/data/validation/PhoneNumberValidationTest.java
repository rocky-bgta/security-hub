package com.aspire.asat.registration.data.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ValidPhoneNumber validation annotation
 */
class PhoneNumberValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidInternationalPhoneNumber() {
        TestDto dto = new TestDto();
        dto.setPhoneNumber("+8801700000000");
        dto.setCountry("Bangladesh");

        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid international phone number should pass validation");
    }

    @Test
    void testInvalidPhoneNumberFormat() {
        TestDto dto = new TestDto();
        dto.setPhoneNumber("8801700000000"); // Missing + prefix
        dto.setCountry("Bangladesh");

        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Invalid phone number format should fail validation");
        assertTrue(violations.iterator().next().getMessage().contains("Phone number"));
    }

    @Test
    void testValidUSPhoneNumber() {
        TestDto dto = new TestDto();
        dto.setPhoneNumber("+12125551234");
        dto.setCountry("United States");

        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid US phone number should pass validation");
    }

    @Test
    void testInvalidPhoneNumberTooShort() {
        TestDto dto = new TestDto();
        dto.setPhoneNumber("+1212555"); // Too short (only 4 digits after country code)
        dto.setCountry("United States");

        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Too short phone number should fail validation");
    }

    @Test
    void testValidUKPhoneNumber() {
        TestDto dto = new TestDto();
        dto.setPhoneNumber("+442071234567");
        dto.setCountry("United Kingdom");

        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid UK phone number should pass validation");
    }

    @Test
    void testGenericInternationalFormat() {
        TestDto dto = new TestDto();
        dto.setPhoneNumber("+1234567890123"); // Generic international format
        dto.setCountry("Any Country");

        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Generic international format should pass validation");
    }

    @Test
    void testNullValuesPassValidation() {
        TestDto dto = new TestDto();
        dto.setPhoneNumber(null);
        dto.setCountry(null);

        Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Null values should pass validation (handled by @NotBlank)");
    }

    @Test
    void testSpecificCountryNames() {
        // Test with the exact country names from the user's list
        String[] countries = {"UK", "Bangladesh", "India", "Canada", "USA"};
        String[] validPhones = {"+442071234567", "+8801700000000", "+919876543210", "+12125551234", "+12125551234"};
        
        for (int i = 0; i < countries.length; i++) {
            TestDto dto = new TestDto();
            dto.setPhoneNumber(validPhones[i]);
            dto.setCountry(countries[i]);

            Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty(), 
                String.format("Valid phone number for %s should pass validation", countries[i]));
        }
    }

    @Test
    void testInvalidPhoneWithSpecificCountries() {
        // Test invalid phones with the exact country names
        String[] countries = {"UK", "Bangladesh", "India", "Canada", "USA"};
        
        for (String country : countries) {
            TestDto dto = new TestDto();
            dto.setPhoneNumber("8801700000000"); // Missing + prefix
            dto.setCountry(country);

            Set<ConstraintViolation<TestDto>> violations = validator.validate(dto);
            assertFalse(violations.isEmpty(), 
                String.format("Invalid phone number for %s should fail validation", country));
        }
    }

    // Test DTO class
    @ValidPhoneNumber
    public static class TestDto {
        private String phoneNumber;
        private String country;

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }
}
