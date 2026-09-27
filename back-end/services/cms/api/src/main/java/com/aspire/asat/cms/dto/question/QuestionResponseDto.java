package com.aspire.asat.cms.dto.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponseDto {
    private String id;
    private String topicId;
    private QuestionTypes questionType;
    private String questionText;
    private List<QuestionOptionDto> options;
    private QuestionStatus status;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
}
