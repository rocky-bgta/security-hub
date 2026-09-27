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
@Schema(description = "Product row for MSP product catalog with assignment status")
public class MspProductCatalogItemDto {

    private String productId;
    private String productName;
    private String productDescription;
    private String thumbnailUrl;
    private Integer displayOrder;

    @Schema(description = "MSP assignment status: ENABLED if MSP has an active license, otherwise DISABLED")
    private String mspProductStatus;

    private String mspProductId;
    private String licenseStatus;
    private List<MspProductCatalogPackageDto> packages;
}
