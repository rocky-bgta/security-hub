package com.aspire.asat.cms.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicQuestionDistributionDto {
    
    @NotBlank(message = "Topic ID is required")
    private String topicId;
    
    @Positive(message = "Question count must be positive")
    private Integer questionCount;
}
