package com.aspire.asat.universal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "poll_survey_submissions")
@CompoundIndex(name = "user_poll_unique", def = "{ 'userId': 1, 'pollSurveyId': 1 }", unique = true)
public class PollSurveySubmission {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    private String userId;

    private UUID pollSurveyId;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

