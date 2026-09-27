package com.aspire.asat.registration.data.clientAdmin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
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
@Schema(description = "Request payload for reassigning products to an existing client")
public class ClientProductAssignment {

    @NotNull(message = "Client Admin ID is required")
    @Schema(
            description = "Client Admin ID for the existing client",
            example = "client-admin-uuid-123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String clientAdminId;

    @Schema(
            description = "MSP ID for the client (optional - will use client admin's existing MSP ID if not provided)",
            example = "msp-uuid-123",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String mspId;

    @NotNull(message = "Product selections are required")
    @Valid
    @Schema(
            description = "List of products to reassign to the client",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private List<@NotNull @Valid ProductSelectionDto> productSelections;

    @NotNull(message = "Invoice details are required")
    @Valid
    @Schema(
            description = "Invoice summary for the reassigned products",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private InvoiceDetailsDto invoice;
}
