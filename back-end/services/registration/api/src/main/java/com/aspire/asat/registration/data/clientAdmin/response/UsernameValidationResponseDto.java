package com.aspire.asat.registration.data.clientAdmin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsernameValidationResponseDto {

    @Schema(description = "The email or username that was checked", example = "admin@example.com")
    private String email;

    @Schema(description = "True if the email/username already exists", example = "true")
    private boolean exists;
}
