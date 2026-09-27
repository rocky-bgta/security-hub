package com.aspire.asat.cms.model.question;

import com.aspire.asat.cms.dto.question.QuestionOptionDto;
import com.aspire.asat.cms.dto.question.QuestionStatus;
import com.aspire.asat.cms.dto.question.QuestionTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "question")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
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
