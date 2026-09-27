package com.aspire.asat.registration.data.invoice;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response structure for Invoice details")
public class InvoiceResponseDTO {

    @Schema(description = "Unique invoice ID", example = "INV-1751887035235")
    private String id;

    @Schema(description = "Client Admin ID associated with the invoice", example = "a1b2c3d4e5")
    private String clientAdminId;

    @Schema(description = "List of ClientProduct IDs linked to this invoice")
    private List<String> clientProductIds;

    @Schema(description = "Subtotal before discounts and VAT", example = "1000.00")
    private double subtotal;

    @Schema(description = "Total discount amount applied", example = "100.00")
    private double discountAmount;

    @Schema(description = "VAT amount applied", example = "45.00")
    private double vatAmount;

    @Schema(description = "Final total amount after applying discount and VAT", example = "945.00")
    private double totalAmount;

    @Schema(description = "Readable invoice download link (PDF)", example = "https://blob.azure.com/invoices/INV-12345.pdf")
    private String invoicePdfLink;

    @Schema(description = "Status of the invoice", example = "PAID")
    private InvoiceStatus status;

    @Schema(description = "Invoice creation timestamp", example = "2025-07-06T10:15:30Z")
    private Instant createdAt;

    @Schema(description = "Timestamp when the invoice was marked as paid", example = "2025-07-06T12:00:00Z")
    private Instant paidAt;

    @Schema(description = "Global unpaid invoice expiry timestamp", example = "2025-08-05T10:15:30Z")
    private Instant expiresAt;

}
