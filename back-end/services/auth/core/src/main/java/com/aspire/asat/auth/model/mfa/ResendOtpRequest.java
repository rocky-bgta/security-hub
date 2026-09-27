package com.aspire.asat.auth.model.mfa;

import com.aspire.asat.auth.dto.enums.MfaMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for resending OTP
 * Only applicable for SMS, EMAIL, and PHONE_CALL methods
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResendOtpRequest {

    @NotNull(message = "Method is required")
    private MfaMethod method;

    /**
     * Phone number (optional for SMS/PHONE_CALL).
     * When omitted, the phone number from the user profile is used.
     */
    private String phoneNumber;

    /**
     * Temporary token for login MFA. Logged-in add-method may omit this and send Authorization Bearer instead.
     */
    private String tempToken;
}

