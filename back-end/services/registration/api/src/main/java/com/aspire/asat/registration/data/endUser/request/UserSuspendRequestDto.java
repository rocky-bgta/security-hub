package com.aspire.asat.registration.data.endUser.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for suspending or activating a user")
public class UserSuspendRequestDto {

    @NotBlank(message = "Status is required")
    @Schema(description = "User status - must be 'SUSPEND' or 'ACTIVE'", example = "SUSPEND", allowableValues = {"SUSPEND", "ACTIVE"}, required = true)
    private String status;

    @Schema(description = "Suspend reason optional when status is ACTIVE)", example = "uuid-of-suspend-reason")
    private String suspendReason;
}

