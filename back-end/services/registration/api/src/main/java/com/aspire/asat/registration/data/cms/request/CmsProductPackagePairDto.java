package com.aspire.asat.registration.data.cms.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product and package ID pair for CMS topic lookup")
public class CmsProductPackagePairDto {

    private String productId;
    private String packageId;
}
