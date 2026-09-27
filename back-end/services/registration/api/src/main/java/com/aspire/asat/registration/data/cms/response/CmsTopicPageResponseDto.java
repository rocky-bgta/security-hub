package com.aspire.asat.registration.data.cms.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsTopicPageResponseDto {
    private int offset;
    private int pageSize;
    /** Topics assigned to the MSP (matching product/package pairs). */
    private long total;
    /** Topics not assigned to the MSP. */
    private long totalLocked;
    private List<CmsTopicMinimalDto> items;
}
