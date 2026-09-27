package com.aspire.asat.billing.dto.mspUser.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO containing billing contact details")
public class BillingInfoForMspDto {

    @NotBlank(message = "Billing email is required")
    @Email(message = "Billing email must be a valid email address")
    @Schema(
            description = "Billing contact email address",
            example = "billing@company.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String billingEmail;

    @NotBlank(message = "Billing name is required")
    @Schema(
            description = "Full name of the billing contact person or entity",
            example = "John Doe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String billingName;
    
    
    @NotBlank(message = "Billing Address is required")
    @Schema(
            description = "Physical billing address",
            example = "123 Business Street, Suite 100, City, State 12345",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String billingAddress;

}
