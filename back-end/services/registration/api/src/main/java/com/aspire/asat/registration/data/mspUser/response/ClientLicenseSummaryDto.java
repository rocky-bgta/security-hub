package com.aspire.asat.registration.data.mspUser.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Client license summary for MSP Client License Management list")
public class ClientLicenseSummaryDto {

    @Schema(description = "Client (Client Admin) ID", example = "client-admin-uuid-123")
    private String clientId;

    @Schema(description = "Client/organization name", example = "Acme Corp")
    private String clientName;

    @Schema(description = "Contact email", example = "admin@acme.com")
    private String contactEmail;

    @Schema(description = "Total licenses allocated", example = "50")
    private Integer licensesAllocated;

    @Schema(description = "Licenses currently in use", example = "35")
    private Integer licensesInUse;

    @Schema(description = "Remaining licenses available", example = "15")
    private Integer remainingLicenses;

    @Schema(description = "Earliest license expiry date across products (if applicable)")
    private Instant licenseExpiryDate;

    @Schema(description = "Client status", example = "ACTIVE")
    private String status;

    @Schema(description = "Client creation timestamp")
    private Instant createdAt;
}
