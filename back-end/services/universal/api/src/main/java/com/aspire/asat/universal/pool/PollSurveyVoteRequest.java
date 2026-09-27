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
public class PollSurveyVoteRequest {
    private List<QuestionVote> votes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionVote {
        private UUID questionId;
        private List<UUID> answerIds; // For RADIO, MCQ, RATING
        private String textAnswer;    // For SHORT_TEXT, LONG_TEXT
    }
}

