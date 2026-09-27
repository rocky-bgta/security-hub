package com.aspire.asat.registration.data.auth.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for password reset token response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetTokenResponseDto {
    private String id;
    private String token;
    private String username;
    private Instant expiryDate;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean used;
    private String createdBy;
    private String updatedBy;
}

