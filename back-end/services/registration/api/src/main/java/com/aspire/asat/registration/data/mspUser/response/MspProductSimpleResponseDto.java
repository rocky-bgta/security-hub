package com.aspire.asat.registration.data.mspUser.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple response DTO for MSP product listing (mirrors CMS ClientProductSimpleResponse).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Simple MSP product listing with essential CMS product fields")
public class MspProductSimpleResponseDto {

    @Schema(description = "Product ID from CMS", example = "product-456")
    private String id;

    @Schema(description = "Product name", example = "Phishing Simulation")
    private String productName;

    @Schema(description = "Product thumbnail URL")
    private String thumbnailUrl;

    @Schema(description = "Display order for sorting", example = "1")
    private Integer displayOrder;
}
