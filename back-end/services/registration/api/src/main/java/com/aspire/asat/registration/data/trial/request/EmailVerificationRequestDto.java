package com.aspire.asat.registration.data.trial.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for email verification - Step 2: Verify Email")
public class EmailVerificationRequestDto {

    @NotBlank(message = "Email address is required")
    @Email(message = "Email must be valid")
    @Schema(description = "Email address to verify", example = "mdtest001@yopmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Verification code is required")
    @Pattern(regexp = "^\\d{6}$", message = "Verification code must be 6 digits")
    @Schema(description = "6-digit verification code sent to email", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String verificationCode;
}

