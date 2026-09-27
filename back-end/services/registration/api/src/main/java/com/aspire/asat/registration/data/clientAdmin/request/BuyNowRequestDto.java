package com.aspire.asat.registration.data.clientAdmin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Request payload for buy now flow - updates existing client admin account, adds products, and creates invoice")
public class BuyNowRequestDto {

    @NotBlank(message = "Client admin ID is required")
    @Schema(
            description = "ID of the existing client admin",
            example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String clientAdminId;

    @Valid
    @Schema(
            description = "Organization details to update (optional - only provided fields will be updated)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private OrganizationInfoDto organization;

    @Valid
    @Schema(
            description = "Billing contact details to update (optional - only provided fields will be updated)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private BillingInfoDto billing;

    @Schema(
            description = "MSP ID (optional - will use existing if not provided)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String mspId;

    @Schema(
            description = "MSP Name (optional - will use existing if not provided)",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String mspName;

    @NotNull(message = "Product selections are required")
    @Schema(
            description = "Selected products and license details to add",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<@NotNull ProductSelectionDto> productSelections;

    @NotNull(message = "Invoice details are required")
    @Valid
    @Schema(
            description = "Invoice summary including discounts and VAT",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private InvoiceDetailsDto invoice;
}

