package com.aspire.asat.registration.data.mspUser.response;

import com.aspire.asat.registration.data.clientAdmin.response.ClientProductDetailedDto;
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
@Schema(description = "Detailed license information for a single client (Client Admin)")
public class MspClientLicenseDetailResponseDto {

    @Schema(description = "Client Admin ID")
    private String clientId;

    @Schema(description = "Client/organization name")
    private String clientName;

    @Schema(description = "Contact email")
    private String contactEmail;

    @Schema(description = "Client status", example = "ACTIVE")
    private String status;

    @Schema(description = "Total licenses allocated (sum across all products)")
    private Integer totalLicensesAllocated;

    @Schema(description = "Licenses in use (sum across all products)")
    private Integer licensesInUse;

    @Schema(description = "Remaining licenses")
    private Integer remainingLicenses;

    @Schema(description = "Product-level license details with product/package info")
    private List<ClientProductDetailedDto> productDetails;
}
