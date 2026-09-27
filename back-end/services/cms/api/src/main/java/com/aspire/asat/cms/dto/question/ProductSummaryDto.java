package com.aspire.asat.cms.dto.question;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Simplified product DTO with only essential fields")
public class ProductSummaryDto {

    @Schema(description = "The ID of the product", example = "product-001")
    private String productId;

    @Schema(description = "The name of the product", example = "Cybersecurity Training")
    private String productName;

    @Schema(description = "The description of the product", example = "Comprehensive cybersecurity training program")
    private String productDescription;
}
