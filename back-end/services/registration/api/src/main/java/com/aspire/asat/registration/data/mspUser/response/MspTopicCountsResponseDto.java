package com.aspire.asat.registration.data.mspUser.response;

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
@Schema(description = "MSP topic distribution for current year (same shape as CMS /topics/distribution)")
public class MspTopicCountsResponseDto {

    @Schema(description = "12 monthly items with totalContent (MSP packages) and usedContent (client packages)")
    private List<MspTopicDistributionItemDto> data;
}
