package com.aspire.asat.universal.entity;

import com.aspire.asat.universal.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollSurveyQuestion {

    @Builder.Default
    private UUID id = UUID.randomUUID();

    private String questionText;

    @Builder.Default
    private QuestionType questionType = QuestionType.RADIO;

    // the parent PollSurvey document will set pollSurveyId when embedding
    private UUID pollSurveyId;

    @Builder.Default
    private List<PollSurveyAnswer> answers = new ArrayList<>();

    public void addAnswer(PollSurveyAnswer a) {
        a.setQuestionId(this.id);
        this.answers.add(a);
    }
}
