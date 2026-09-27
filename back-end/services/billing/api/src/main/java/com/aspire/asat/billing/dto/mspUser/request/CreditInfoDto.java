package com.aspire.asat.billing.dto.mspUser.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Credit allocation details for the MSP")
public class CreditInfoDto {

    @DecimalMin(value = "0.0", inclusive = true, message = "Credit amount must be non-negative")
    @Schema(description = "Initial credit amount for the MSP", example = "1000.00")
    private Double creditAmount;

    @NotBlank
    @Schema(description = "Reason for providing the credit", example = "Initial credit allocation for launch")
    private String reason;
}
