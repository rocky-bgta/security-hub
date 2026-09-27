package com.aspire.asat.universal.pool;

import com.aspire.asat.universal.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollSurveyQuestionCreateDto {
    private String questionText;
    private QuestionType questionType;
    private List<PollSurveyAnswerCreateDto> answers;
}

