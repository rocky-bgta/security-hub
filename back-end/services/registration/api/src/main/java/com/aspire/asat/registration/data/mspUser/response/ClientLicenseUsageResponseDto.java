package com.aspire.asat.registration.data.mspUser.response;

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
@Schema(description = "Client license usage details for MSP view")
public class ClientLicenseUsageResponseDto {

    @Schema(description = "Client Admin ID")
    private String clientAdminId;

    @Schema(description = "Total licenses currently in use across all products")
    private Integer licensesInUse;

    @Schema(description = "License usage history (allocated, removed, etc.)")
    private List<LicenseUsageHistoryItemDto> usageHistory;

    @Schema(description = "Count of allocated but inactive (unused) licenses")
    private Integer inactiveLicenseCount;

    @Schema(description = "Products with licenses expiring within the threshold (e.g. 30 days)")
    private List<ClientProductExpiryItemDto> upcomingExpirations;
}
