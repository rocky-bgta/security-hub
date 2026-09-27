package com.aspire.asat.cms.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * Entity representing client-specific exam settings.
 * Stores configuration for time limits, passing criteria, retake policies, and exam creation settings per client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "exam_settings")
public class ExamSettings {

    @Id
    private String id;

    private String clientId;
    private Integer timeLimitMinutes;
    private Integer passingScore;
    private String retakePolicy;
    
    // New fields for exam creation
    private Integer totalQuestions;
    private String distributionStrategy; // EQUAL, CUSTOM, WEIGHTED
    private List<TopicQuestionDistribution> customDistribution;
    
    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isActive;
    private String createdBy;
    private String updatedBy;
    private Boolean defaultSettings;
    
    /**
     * Nested class for custom distribution configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicQuestionDistribution {
        private String topicId;
        private Integer questionCount;
    }
}
