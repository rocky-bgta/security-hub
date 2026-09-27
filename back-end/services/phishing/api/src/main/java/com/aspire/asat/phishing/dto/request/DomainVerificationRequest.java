package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for verifying a domain with the OTP code.
 * Based on BRD Use Case 2.1.1.1 - Step 5: Admin Inputs Verification Code
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainVerificationRequest {
    
    @NotBlank(message = "Email address is required")
    @Email(message = "Please enter a valid email address")
    private String emailAddress;
    
    @NotBlank(message = "Verification code is required")
    @Size(min = 6, max = 8, message = "Verification code must be 6-8 characters")
    private String verificationCode;
}
