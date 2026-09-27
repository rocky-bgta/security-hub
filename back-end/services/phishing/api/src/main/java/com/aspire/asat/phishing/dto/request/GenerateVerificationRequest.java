package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for generating and sending a domain verification email.
 * Based on BRD Use Case 2.1.1.1 - Step 2: Admin Initiates Domain Verification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateVerificationRequest {
    
    @NotBlank(message = "Email address is required")
    @Email(message = "Please enter a valid email address")
    private String emailAddress;
}
