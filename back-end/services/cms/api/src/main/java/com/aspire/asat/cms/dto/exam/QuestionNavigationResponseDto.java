package com.aspire.asat.cms.dto.exam;

import com.aspire.asat.cms.dto.question.QuestionTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionNavigationResponseDto {

    private String examId;
    private String questionId;
    private int currentQuestionNumber;
    private int totalQuestions;
    private String questionText;
    private List<String> options;
    private QuestionTypes questionType;
    private boolean hasNext;
    private boolean hasPrevious;
    private Integer nextQuestionNumber;
    private Integer previousQuestionNumber;
}
