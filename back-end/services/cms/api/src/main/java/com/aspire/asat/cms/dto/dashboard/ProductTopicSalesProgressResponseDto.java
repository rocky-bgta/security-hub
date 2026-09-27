package com.aspire.asat.cms.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for sales progress by product-topics according to month
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTopicSalesProgressResponseDto {
    
    private String chartTitle;
    private String timeFrame;
    private List<String> productCategories;
    private List<Map<String, Object>> seriesData;
}

