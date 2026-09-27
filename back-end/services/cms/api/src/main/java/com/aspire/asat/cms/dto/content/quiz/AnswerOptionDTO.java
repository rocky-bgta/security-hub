package com.aspire.asat.cms.dto.content.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerOptionDTO {
    private String optionText;
    private String optionMediaUrl;

    private Boolean isCorrect;

    private String matchingValue;
    private Integer orderIndex;
}
