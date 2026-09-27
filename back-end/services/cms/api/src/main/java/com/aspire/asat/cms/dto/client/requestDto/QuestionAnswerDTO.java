package com.aspire.asat.cms.dto.client.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswerDTO {

    @NotBlank
    private String questionId;

    @NotEmpty
    private List<String> providedAnswers;  // User's answers (supports multiple for multiple-choice)
}
