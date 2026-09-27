package com.aspire.asat.cms.dto.clientExamSettings;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating ClientExamSettings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSettingsCreateRequestDto {

    @NotNull(message = "Client ID is required")
    private String clientId;

    @NotNull(message = "Time limit is required")
    @Positive(message = "Time limit must be positive")
    private Integer timeLimitMinutes;

    @NotNull(message = "Passing score is required")
    @PositiveOrZero(message = "Passing score must be non-negative")
    private Integer passingScore;

    @NotNull(message = "Retake policy is required")
    @Size(min = 1, max = 50, message = "Retake policy must be between 1 and 50 characters")
    private String retakePolicy;
    
    // New fields for exam creation
    @NotNull(message = "Total questions is required")
    @Positive(message = "Total questions must be positive")
    private Integer totalQuestions;
    
    @NotNull(message = "Distribution strategy is required")
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
        @NotNull(message = "Topic ID is required")
        private String topicId;
        
        @NotNull(message = "Question count is required")
        @Positive(message = "Question count must be positive")
        private Integer questionCount;
    }
}
