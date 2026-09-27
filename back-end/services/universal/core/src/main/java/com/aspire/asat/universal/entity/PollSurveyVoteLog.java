package com.aspire.asat.universal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "poll_survey_vote_logs")
// non-unique compound index to speed up lookups by user and poll; uniqueness is enforced in service layer
@CompoundIndex(name = "user_poll_idx", def = "{ 'userId': 1, 'pollSurveyId': 1 }")
public class PollSurveyVoteLog {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Indexed
    private String userId;

    @Indexed
    private UUID pollSurveyId;

    @Indexed
    private UUID questionId;

    @Indexed
    private UUID answerId;

    // For text-based questions (SHORT_TEXT, LONG_TEXT)
    private String textAnswer;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
