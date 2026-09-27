package com.aspire.asat.universal.pool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollSurveyAnswerCreateDto {
    private String answerText;
}

