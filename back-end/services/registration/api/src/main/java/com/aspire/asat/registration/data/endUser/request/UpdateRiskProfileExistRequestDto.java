package com.aspire.asat.registration.data.endUser.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for bulk updating risk profile existence flag for users")
public class UpdateRiskProfileExistRequestDto {

    @NotEmpty(message = "User IDs list is required")
    @Schema(description = "List of user IDs to update", example = "[\"uuid-1\", \"uuid-2\"]", required = true)
    private List<String> userIds;

    @NotNull(message = "isRiskProfileExist is required")
    @Schema(description = "Set to true to mark that risk profile exists for the users", example = "true", required = true)
    private Boolean isRiskProfileExist;
}
