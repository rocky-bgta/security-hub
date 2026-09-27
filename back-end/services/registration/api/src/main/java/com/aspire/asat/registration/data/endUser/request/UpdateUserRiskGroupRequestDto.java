package com.aspire.asat.registration.data.endUser.request;

import com.aspire.asat.registration.data.enums.RiskGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for updating user risk group")
public class UpdateUserRiskGroupRequestDto {

    @NotNull(message = "Risk group is required")
    @Schema(
            description = "Risk group classification for the user",
            example = "HIGH_RISK",
            required = true)
    private RiskGroup riskGroup;
}
