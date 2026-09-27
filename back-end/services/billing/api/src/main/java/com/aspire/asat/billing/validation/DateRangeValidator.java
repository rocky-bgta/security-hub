package com.aspire.asat.billing.validation;

import com.aspire.asat.billing.dto.CouponCreateRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Instant;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, CouponCreateRequestDTO> {

    @Override
    public boolean isValid(CouponCreateRequestDTO dto, ConstraintValidatorContext context) {
        Instant from = dto.getValidFrom();
        Instant until = dto.getValidUntil();

        if (from == null || until == null) return true; // Let @NotNull handle null case

        return until.isAfter(from);
    }
}
