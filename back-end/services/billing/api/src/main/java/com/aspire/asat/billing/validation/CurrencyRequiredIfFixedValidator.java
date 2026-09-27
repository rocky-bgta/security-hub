package com.aspire.asat.billing.validation;

import com.aspire.asat.billing.dto.CouponCreateRequestDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CurrencyRequiredIfFixedValidator implements ConstraintValidator<CurrencyRequiredIfFixed, CouponCreateRequestDTO> {

    @Override
    public boolean isValid(CouponCreateRequestDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true; // skip null

        if ("FIXED".equalsIgnoreCase(dto.getType()) && (dto.getCurrency() == null || dto.getCurrency().trim().isEmpty())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Currency must not be blank when type is FIXED")
                    .addPropertyNode("currency")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
