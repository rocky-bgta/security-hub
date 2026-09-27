package com.aspire.asat.registration.data.clientAdmin.response;

import com.aspire.asat.registration.data.invoice.InvoiceResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response returned after successful buy now flow - account updated, products added, and invoice created")
public class BuyNowResponseDto {

    @Schema(description = "Unique identifier of the client admin", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String clientAdminId;

    @Schema(description = "Email address of the client admin", example = "admin@company.com")
    private String adminEmail;

    @Schema(description = "Client organization name", example = "Company Name")
    private String organizationName;

    @Schema(description = "List of newly created client product IDs", example = "[\"product-id-1\", \"product-id-2\"]")
    private java.util.List<String> clientProductIds;

    private InvoiceResponseDTO invoiceResponse;

    @Schema(description = "Link to the client admin portal", example = "https://portal.aspireelearning.com/auth/login")
    private String portalLink;
}

