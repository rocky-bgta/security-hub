package com.aspire.asat.registration.data.cms.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Feature details from CMS service")
public class CmsFeatureDto {

    @Schema(description = "Feature ID", example = "a1160ef3-8593-4cc1-bd8e-d65c1129acab")
    private String id;

    @Schema(description = "Feature name", example = "Support 24h")
    private String name;
}
