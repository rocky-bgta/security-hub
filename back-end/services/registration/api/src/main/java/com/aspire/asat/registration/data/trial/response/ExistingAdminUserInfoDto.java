package com.aspire.asat.registration.data.trial.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Information about existing admin or user")
public class ExistingAdminUserInfoDto {

    @Schema(description = "Name of the admin/user", example = "John Doe")
    private String name;

    @Schema(description = "Email address", example = "john.doe@islamibank.com")
    private String email;

    @Schema(description = "Phone number", example = "+880 1784-669597")
    private String phoneNumber;

    @Schema(description = "Date when the account was created", example = "2024-01-15T10:30:00Z")
    private Instant createdAt;
}
