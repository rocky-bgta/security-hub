package com.aspire.asat.auth.model.mfa;

import com.aspire.asat.auth.dto.TokenShortResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for OTP verification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpResponse {

    private Boolean verified;
    private String message;
    
    /**
     * Access token response (same as Login API response) - included after successful MFA verification
     */
    private TokenShortResponse tokenResponse;

}

