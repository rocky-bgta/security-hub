package com.aspire.asat.cms.dto.client.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAssessmentRequestDTO {
    @NotBlank
    private String questionId;

    @NotNull
    private List<String> providedAnswers;
}
