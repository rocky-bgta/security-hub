package com.aspire.asat.cms.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple DTO for feature information containing id and name
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureDto {
    private String id;
    private String name;
}
