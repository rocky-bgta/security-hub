package com.aspire.asat.registration.data.mspUser.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Package row for MSP product catalog")
public class MspProductCatalogPackageDto {

    private String packageId;
    private String packageName;
    private Double price;
    private String packageStatus;
    private String mspProductId;
    private String licenseStatus;
}
