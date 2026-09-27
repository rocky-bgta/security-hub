package com.aspire.asat.universal.pool;

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
public class PollSurveySummaryResponse {
    private UUID pollSurveyId;
    private String title;
    private List<QuestionSummary> questions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionSummary {
        private UUID questionId;
        private String questionText;
        private long totalVotes;
        private List<AnswerSummary> answers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerSummary {
        private UUID answerId;
        private String answerText;
        private long voteCount;
        private double percentage;
    }
}

