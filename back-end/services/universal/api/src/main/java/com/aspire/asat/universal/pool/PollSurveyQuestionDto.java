package com.aspire.asat.universal.pool;

import com.aspire.asat.universal.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollSurveyQuestionDto {
    private UUID id;
    private String questionText;
    private QuestionType questionType;
    private List<PollSurveyAnswerDto> answers;
}

