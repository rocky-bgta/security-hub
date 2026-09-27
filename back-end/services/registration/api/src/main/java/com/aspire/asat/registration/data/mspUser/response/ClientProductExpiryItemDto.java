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
@Schema(description = "Client product with expiry info for upcoming expirations")
public class ClientProductExpiryItemDto {

    @Schema(description = "Client product ID")
    private String clientProductId;

    @Schema(description = "Product ID")
    private String productId;

    @Schema(description = "Package ID")
    private String packageId;

    @Schema(description = "License count for this product")
    private Integer licenseCount;

    @Schema(description = "Expiry date")
    private Instant expiryDate;

    @Schema(description = "License status", example = "ACTIVE")
    private String licenseStatus;
}
