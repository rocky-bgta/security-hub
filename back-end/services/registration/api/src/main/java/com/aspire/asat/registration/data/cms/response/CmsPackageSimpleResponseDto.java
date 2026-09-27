package com.aspire.asat.registration.data.cms.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal package response from CMS POST /products/packages/by-ids.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Simple package id and name from CMS")
public class CmsPackageSimpleResponseDto {

    @Schema(description = "Package ID")
    private String id;

    @Schema(description = "Package name")
    private String name;
}
