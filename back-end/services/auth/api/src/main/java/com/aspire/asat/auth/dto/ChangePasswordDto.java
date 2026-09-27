package com.aspire.asat.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDto {
    @NotBlank
    @ToString.Exclude
    private String currentPassword;

    @NotBlank
    @Size(max = 64, message = "Password must not exceed 64 characters")
    @ToString.Exclude
    private String newPassword;

    @NotBlank
    @Size(max = 64, message = "Password must not exceed 64 characters")
    @ToString.Exclude
    private String confirmPassword;
}

