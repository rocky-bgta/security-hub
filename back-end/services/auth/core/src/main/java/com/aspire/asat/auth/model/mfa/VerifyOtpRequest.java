package com.aspire.asat.auth.model.mfa;

import com.aspire.asat.auth.dto.enums.MfaMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Request DTO for verifying MFA code
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {

    @NotNull(message = "Method is required")
    private MfaMethod method;

    @NotBlank(message = "Code is required")
    private String code;

    /**
     * Session ID is required for SMS and EMAIL methods, but not for AUTHENTICATOR method
     */
    private UUID sessionId;

    /**
     * Temporary token for login MFA. Logged-in add-method may omit this and send Authorization Bearer instead.
     */
    private String tempToken;

}

