package com.aspire.asat.auth.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordDto {
    @NotBlank
    private String token;

    @NotBlank
    @Size(max = 64, message = "Password must not exceed 64 characters")
    @ToString.Exclude  // 👈 prevents this field from being printed in logs
    private String newPassword;
}

