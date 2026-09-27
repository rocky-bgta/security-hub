package com.aspire.asat.registration.data.clientAdmin.request;

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
@Schema(description = "Details of the selected MSP partner during onboarding")
public class MspSelectionDto {

    @NotBlank(message = "MSP ID is required")
    @Schema(
            description = "Unique identifier of the selected MSP",
            example = "msp-abc-123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String mspId;

    @NotBlank(message = "MSP name is required")
    @Schema(
            description = "Name of the MSP partner",
            example = "Aspire MSP Partner Ltd.",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String mspName;

    @NotBlank(message = "Country is required")
    @Schema(
            description = "Country where the MSP operates",
            example = "Bangladesh",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String country;

    @NotBlank(message = "Province is required")
    @Schema(
            description = "Province or state where the MSP is located",
            example = "Dhaka",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String province;
}
