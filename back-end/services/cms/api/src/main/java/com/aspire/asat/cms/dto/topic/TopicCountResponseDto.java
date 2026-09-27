package com.aspire.asat.cms.dto.topic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing unique topic count")
public class TopicCountResponseDto {

    @Schema(description = "Count of unique topics matching the product and package IDs", example = "25")
    private Long count;
}
