package com.aspire.asat.registration.data.cms.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Monthly topic distribution item (mirrors CMS TopicDistributionItemDto)")
public class CmsTopicDistributionItemDto {

    private String month;
    private Long totalContent;
    private Long usedContent;
}
