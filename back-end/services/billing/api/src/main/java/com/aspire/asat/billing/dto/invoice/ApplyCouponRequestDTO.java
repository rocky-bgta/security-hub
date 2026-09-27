package com.aspire.asat.billing.dto.invoice;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to apply a coupon to an existing invoice before payment")
public class ApplyCouponRequestDTO {

    @NotBlank
    @Schema(description = "Coupon code to apply", example = "SUMMER10")
    private String couponCode;

    @Schema(description = "Whether to send the updated invoice PDF by email", example = "true")
    private boolean sendEmail;
}
