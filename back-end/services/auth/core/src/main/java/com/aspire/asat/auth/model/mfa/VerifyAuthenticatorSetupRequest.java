package com.aspire.asat.auth.model.mfa;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for verifying authenticator setup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyAuthenticatorSetupRequest {

    @NotBlank(message = "Code is required")
    private String code;

    /**
     * Temporary token for login MFA. Logged-in add-method may omit this and send Authorization Bearer instead.
     */
    private String tempToken;

}

