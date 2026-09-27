package com.aspire.asat.registration.data.mspUser.response;

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
@Schema(description = "Assigned MSP product response matching CMS GET /api/v1/products item shape")
public class MspAssignedProductResponseDto {

    private String productId;
    private String productName;
    private String productDescription;
    private String productStatus;
    private String thumbnailUrl;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
    private String lastModifiedBy;
    private List<MspAssignedPackageResponseDto> packages;
    private Boolean isTrial;
    private Boolean showInSite;
    private Integer displayOrder;
}
