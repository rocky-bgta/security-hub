package com.aspire.asat.registration.data.endUser.request;

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
@Schema(description = "Request payload for migrating trial user to regular user account")
public class MigrateTrialUserRequestDto {

    @NotBlank(message = "New client admin ID is required")
    @Schema(
            description = "ID of the new client admin (regular account)",
            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String clientAdminId;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(
            description = "Email address of the new admin user who is purchasing the plan",
            example = "admin@xcompany.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @Schema(
            description = "Domain name (optional - if provided, will update domain in client admin table)",
            example = "xcompany.com",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String domain;
}
