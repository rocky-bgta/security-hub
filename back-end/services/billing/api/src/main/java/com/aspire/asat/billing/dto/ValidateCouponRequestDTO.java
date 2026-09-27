package com.aspire.asat.billing.dto;

import com.aspire.asat.billing.dto.invoice.ProductSelectionDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "Request for validating a coupon before applying it")
public class ValidateCouponRequestDTO {

    @Schema(description = "Coupon ID to be validated", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String couponId;

    @Schema(description = "Coupon code to be validated", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String couponCode;

    @Schema(description = "Total purchase amount", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double amount;

    @Schema(
            description = "Optional: List of package items being purchased",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<ProductSelectionDto> packageItems;

    @Schema(
            description = "Optional: Client country ID (UUID format)",
            example = "ad30c701-9827-4fb7-b404-fac01fed5e77"
    )
    private String clientCountryId;
}
