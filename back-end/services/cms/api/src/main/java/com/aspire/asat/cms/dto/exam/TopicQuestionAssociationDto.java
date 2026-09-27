package com.aspire.asat.cms.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicQuestionAssociationDto {
    
    @NotBlank(message = "Topic ID is required")
    private String topicId;
    
    @NotNull(message = "Question ID is required")
    private UUID questionId;
    
    @NotBlank(message = "Created by is required")
    private String createdBy;
}
