package com.aspire.asat.registration.data.clientAdmin.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Full payload required to onboard a client admin")
public class ClientOnboardingRequestDto {

    @NotNull
    @Schema(description = "Organization details", requiredMode = Schema.RequiredMode.REQUIRED)
    private OrganizationInfoDto organization;

    @NotNull
    @Schema(description = "Billing contact details", requiredMode = Schema.RequiredMode.REQUIRED)
    private BillingInfoDto billing;

    @NotNull
    @Schema(description = "MSP ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mspId;

    @NotNull
    @Schema(description = "MSP Name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mspName;

    @NotNull
    @Schema(description = "Selected products and license details", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@NotNull ProductSelectionDto> productSelections;

    @NotNull
    @Schema(description = "Invoice summary including discounts and VAT", requiredMode = Schema.RequiredMode.REQUIRED)
    private InvoiceDetailsDto invoice;

}
