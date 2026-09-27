package com.aspire.asat.registration.data.mspUser.request;

import com.aspire.asat.registration.data.clientAdmin.request.InvoiceDetailsDto;
import com.aspire.asat.registration.data.clientAdmin.request.ProductSelectionDto;
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
@Schema(description = "Full payload required to onboard an MSP user")
public class MspOnboardingRequestDto {

    @NotNull
    @Schema(description = "Organization details", requiredMode = Schema.RequiredMode.REQUIRED)
    private OrganizationInfoForMspDto organization;

    @NotNull
    @Schema(description = "Billing contact details", requiredMode = Schema.RequiredMode.REQUIRED)
    private BillingInfoForMspDto billing;

    @NotNull
    @Schema(description = "Selected products and license details", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@NotNull ProductSelectionDto> productSelections;

    @NotNull
    @Schema(description = "Invoice summary including discounts and VAT", requiredMode = Schema.RequiredMode.REQUIRED)
    private InvoiceDetailsDto invoice;

    @Schema(description = "Credit allocation details for the MSP")
    private CreditInfoDto creditInfo;
}
