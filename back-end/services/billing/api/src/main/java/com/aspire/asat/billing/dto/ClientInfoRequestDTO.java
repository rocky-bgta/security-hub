package com.aspire.asat.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;



@Data
@Schema(description = "DTO for capturing client information for manual payment onboarding")
public class ClientInfoRequestDTO {

    @Schema(
            description = "Full name of the client",
            example = "John Doe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Client name is required")
    private String clientName;

    @Schema(
            description = "Valid contact email address",
            example = "john.doe@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be valid")
    private String contactEmail;

    @Schema(
            description = "Client's phone number",
            example = "+8801700000000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Phone number is required")
    private String phone;

    @Schema(
            description = "Registered business name",
            example = "Doe Enterprises Ltd.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Business name is required")
    private String businessName;

    @Schema(
            description = "Business or residential address of the client",
            example = "123 Main Street, Dhaka, Bangladesh",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Address is required")
    private String address;
}
