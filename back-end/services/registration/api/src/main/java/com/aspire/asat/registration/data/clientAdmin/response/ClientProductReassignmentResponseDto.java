package com.aspire.asat.registration.data.clientAdmin.response;

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
@Schema(description = "Response returned after successful product reassignment to client")
public class ClientProductReassignmentResponseDto {

    @Schema(description = "Client admin ID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String clientAdminId;

    @Schema(description = "Client admin email", example = "admin@aspiredigital.com")
    private String clientAdminEmail;

    @Schema(description = "Organization name", example = "Aspire Digital Ltd.")
    private String organizationName;

    @Schema(description = "List of newly assigned product IDs", example = "[\"prod-123\", \"prod-456\"]")
    private List<String> assignedProductIds;

    @Schema(description = "Total number of products reassigned", example = "2")
    private Integer totalProductsReassigned;

    @Schema(description = "Total amount for reassigned products", example = "1890.00")
    private Double totalAmount;

    @Schema(description = "Invoice ID for the reassignment", example = "INV-REASSIGN-123")
    private String invoiceId;

    @Schema(description = "Portal link for client access", example = "https://portal.aspireelearning.com/auth/login")
    private String portalLink;
}
