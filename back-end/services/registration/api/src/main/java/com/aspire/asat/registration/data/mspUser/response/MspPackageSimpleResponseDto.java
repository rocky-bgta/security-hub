package com.aspire.asat.registration.data.mspUser.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple response DTO for MSP package listing (mirrors CMS ClientPackageSimpleResponse).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Package information for an MSP and product")
public class MspPackageSimpleResponseDto {

    @Schema(description = "Package ID", example = "package-uuid-123")
    private String id;

    @Schema(description = "Package name", example = "Premium Package")
    private String name;
}
