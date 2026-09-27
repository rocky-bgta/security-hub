package com.aspire.asat.auth.model.mfa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for authenticator setup (QR code generation)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatorSetupResponse {

    private String qrCodeUrl; // Data URL for QR code image (base64)
    private String secret; // Base32 encoded secret (for manual entry)
    private String otpauthUri; // otpauth:// URI for authenticator apps

}

