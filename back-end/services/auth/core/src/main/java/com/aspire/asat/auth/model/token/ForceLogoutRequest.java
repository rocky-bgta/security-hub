package com.aspire.asat.auth.model.token;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for forced logout by userId (service-to-service)")
public class ForceLogoutRequest {

    @NotBlank(message = "userId is required")
    @Schema(description = "Target user ID whose session should be revoked", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userId;
}
