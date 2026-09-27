package com.aspire.asat.registration.data.mspUser.response;

import com.aspire.asat.registration.data.invoice.InvoiceResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response returned after successful MSP buy-now flow")
public class MspBuyNowResponseDto {

    @Schema(description = "MSP ID")
    private String mspId;

    @Schema(description = "MSP admin email")
    private String adminEmail;

    @Schema(description = "MSP organization name")
    private String organizationName;

    @Schema(description = "Newly created MSP product IDs")
    private List<String> mspProductIds;

    private InvoiceResponseDTO invoiceResponse;

    @Schema(description = "MSP portal login link")
    private String portalLink;
}
