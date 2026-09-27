package com.aspire.asat.cms.dto.topic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to count unique topics by product and package IDs")
public class TopicCountByProductPackageRequest {

    @Schema(description = "List of product IDs")
    private List<String> productIds;

    @Schema(description = "List of package IDs")
    private List<String> packageIds;
}
