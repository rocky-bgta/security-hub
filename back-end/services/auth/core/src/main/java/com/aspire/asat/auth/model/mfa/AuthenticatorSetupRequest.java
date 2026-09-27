package com.aspire.asat.auth.model.mfa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for authenticator setup
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatorSetupRequest {
    /**
     * Temporary token for MFA flow (optional - can use Authorization header instead)
     */
    private String tempToken;
}

