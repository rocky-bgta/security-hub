package com.aspire.asat.registration.data.mspUser.request;

import com.aspire.asat.registration.data.clientAdmin.request.InvoiceDetailsDto;
import com.aspire.asat.registration.data.clientAdmin.request.ProductSelectionDto;
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
@Schema(description = "Request payload for MSP buy-now flow - adds products to existing MSP and creates invoice")
public class MspBuyNowRequestDto {

    @NotBlank(message = "MSP ID is required")
    @Schema(description = "ID of the existing MSP", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mspId;

    @Valid
    @Schema(description = "Billing contact details (optional - uses MSP profile billing if omitted)")
    private BillingInfoForMspDto billing;

    @NotNull(message = "Product selections are required")
    @Schema(description = "Selected products and license details to add", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@NotNull ProductSelectionDto> productSelections;

    @NotNull(message = "Invoice details are required")
    @Valid
    @Schema(description = "Invoice summary including discounts and VAT", requiredMode = Schema.RequiredMode.REQUIRED)
    private InvoiceDetailsDto invoice;
}
