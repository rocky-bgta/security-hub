package com.aspire.asat.registration.data.clientAdmin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response returned after successful onboarding of a client admin")
public class ClientOnboardingResponseDto {

    @Schema(description = "Unique identifier of the onboarded client admin", example = "a1b2c3d4")
    private String adminId;

    @Schema(description = "Email address used as the username for login", example = "admin@bankasia.com")
    private String adminEmail;
    
    @Schema(description = "Temporary password generated for initial login", example = "Temp@1234")
    private String tempPassword;

    @Schema(description = "Client organization ID", example = "org-84921")
    private String organizationId;

    @Schema(description = "Client organization name", example = "Bank Asia")
    private String organizationName;

    @Schema(description = "Link to the client admin portal", example = "https://portal.aspireelearning.com/auth/login")
    private String portalLink;

}
