package com.aspire.asat.registration.data.cms.response;

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
@Schema(description = "CMS monthly topic distribution for MSP vs client product/package pairs")
public class CmsTopicCountsByProductPackagesResponseDto {

    private List<CmsTopicDistributionItemDto> data;
}
