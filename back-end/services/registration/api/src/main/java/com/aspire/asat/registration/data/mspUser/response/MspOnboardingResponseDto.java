package com.aspire.asat.registration.data.mspUser.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after successful MSP onboarding")
public class MspOnboardingResponseDto {

    @Schema(description = "Generated unique ID of the MSP user", example = "uuid-abc-123")
    private String adminId;

    @Schema(description = "Email used for MSP admin login", example = "admin@mspdomain.com")
    private String adminEmail;

    @Schema(description = "Temporary password assigned to the MSP admin", example = "Temp@2431")
    private String tempPassword;

    @Schema(description = "Organization ID generated during onboarding", example = "org-xyz-789")
    private String organizationId;

    @Schema(description = "Organization name", example = "MSP Global Networks Ltd.")
    private String organizationName;

    @Schema(description = "Portal login link", example = "https://portal.aspireelearning.com/auth/login")
    private String portalLink;

    @Schema(description = "Optional message to be shown after onboarding", example = "Onboarding complete, email sent")
    private String message;
}
