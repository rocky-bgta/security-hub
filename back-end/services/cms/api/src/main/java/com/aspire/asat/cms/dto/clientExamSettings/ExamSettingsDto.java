package com.aspire.asat.cms.dto.clientExamSettings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO for ClientExamSettings response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSettingsDto {

    private String id;
    private String clientId;
    private Integer timeLimitMinutes;
    private Integer passingScore;
    private String retakePolicy;
    
    // New fields for exam creation
    private Integer totalQuestions;
    private String distributionStrategy;
    private List<TopicQuestionDistributionDto> customDistribution;

    private Instant createdAt;
    private Instant updatedAt;
    private Boolean isActive;
    private String createdBy;
    private String updatedBy;
    private Boolean defaultSettings;
    
    /**
     * DTO for custom distribution configuration
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopicQuestionDistributionDto {
        private String topicId;
        private Integer questionCount;
    }
}
