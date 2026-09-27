package com.aspire.asat.universal.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollSurveyAnswer {

    @Builder.Default
    private UUID id = UUID.randomUUID();

    private String answerText;

    // reference to parent question when embedded
    private UUID questionId;

}
