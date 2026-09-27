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
@Schema(description = "Monthly topic distribution for MSP vs client product/package pairs "
        + "(same shape as /topics/distribution)")
public class TopicCountsByProductPackagesResponseDto {

    @Schema(description = "12 monthly items for the current year")
    private List<TopicDistributionItemDto> data;
}
