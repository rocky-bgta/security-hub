package com.aspire.asat.cms.repository.topic;

import com.aspire.asat.cms.dto.topic.TopicDistributionItemDto;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Custom repository for efficient topic distribution queries using MongoDB aggregation
 */
@Repository
public interface TopicDistributionRepositoryCustom {
    
    /**
     * Gets topic distribution statistics for the current year using MongoDB aggregation.
     * This method efficiently calculates:
     * - totalContent: Count of ENABLED topics created in each month
     * - usedContent: Count of unique topics used in subpackages created in each month
     * 
     * @param yearStart Start of the year (January 1, 00:00:00)
     * @param yearEnd End of the year (December 31, 23:59:59)
     * @return List of 12 TopicDistributionItemDto objects, one for each month
     */
    List<TopicDistributionItemDto> getTopicDistribution(Instant yearStart, Instant yearEnd);
}

