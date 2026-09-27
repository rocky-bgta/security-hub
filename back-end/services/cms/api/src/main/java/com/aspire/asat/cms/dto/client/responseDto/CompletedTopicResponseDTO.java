package com.aspire.asat.cms.dto.client.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for completed topic response
 * Contains topic information with completion date
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletedTopicResponseDTO {
    
    /**
     * Unique identifier of the topic
     */
    private String topicId;
    
    /**
     * Name of the topic
     */
    private String topicName;
    
    /**
     * Date when the topic was completed (lastSynced date from UserTopicProgress)
     */
    private Instant completionDate;
}
