package com.aspire.asat.cms.dto.topic;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to count topics for MSP product pairs and client product pairs")
public class TopicCountsByProductPackagesRequest {

    @Valid
    @Schema(description = "Product/package pairs from msp_products (drives totalCount)")
    private List<ProductPackagePairDto> mspProductPackages;

    @Valid
    @Schema(description = "Product/package pairs from client_products (drives usedCount)")
    private List<ProductPackagePairDto> clientProductPackages;
}
