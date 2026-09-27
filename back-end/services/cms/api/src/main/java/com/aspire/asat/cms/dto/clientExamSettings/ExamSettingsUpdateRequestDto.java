package com.aspire.asat.cms.dto.clientExamSettings;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for updating ClientExamSettings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSettingsUpdateRequestDto {

    @Positive(message = "Time limit must be positive")
    private Integer timeLimitMinutes;

    @PositiveOrZero(message = "Passing score must be non-negative")
    private Integer passingScore;

    @Size(min = 1, max = 50, message = "Retake policy must be between 1 and 50 characters")
    private String retakePolicy;
    
    // New fields for exam creation
    @Positive(message = "Total questions must be positive")
    private Integer totalQuestions;
    
    private String distributionStrategy;
    
    private List<TopicQuestionDistributionDto> customDistribution;

    private Boolean isActive;
    
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
