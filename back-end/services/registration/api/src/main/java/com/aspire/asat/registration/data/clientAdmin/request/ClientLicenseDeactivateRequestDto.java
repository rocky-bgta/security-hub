package com.aspire.asat.registration.data.clientAdmin.request;

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
@Schema(description = "Request to deactivate client licenses after coupon-expiry invoice cancellation")
public class ClientLicenseDeactivateRequestDto {

    @Schema(description = "Client product IDs from the cancelled invoice to set INACTIVE")
    private List<String> clientProductIds;
}
