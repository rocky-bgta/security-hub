package com.aspire.asat.universal.pool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollSurveyAnswerDto {
    private UUID id;
    private String answerText;
}

