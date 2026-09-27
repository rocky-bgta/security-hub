package com.aspire.asat.cms.dto.content.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionDto {
    private QuestionType type;

    private String text;
    private String mediaUrl;

    private DifficultyLevel difficulty;
    private Integer weight;

    private String feedbackCorrect;
    private String feedbackIncorrect;
    private List<AnswerOptionDTO> options;

    private Map<String, String> additionalProperties;
    private Map<String, Object> metadata;
}
