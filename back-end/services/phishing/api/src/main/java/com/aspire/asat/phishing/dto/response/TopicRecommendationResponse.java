package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.cms.CmsTopicRespDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response wrapper for the topic recommendation endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicRecommendationResponse {

    private List<CmsTopicRespDto> topics;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private boolean hasNext;
    private boolean hasPrevious;
}
