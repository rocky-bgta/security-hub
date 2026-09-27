package com.aspire.asat.cms.dto.exam;

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
public class IndividualQuestionSubmissionDto {

    @NotBlank(message = "Exam ID must not be blank")
    private String examId;

    @NotBlank(message = "User ID must not be blank")
    private String userId;

    @NotBlank(message = "Question ID must not be blank")
    private String questionId;

    @NotEmpty(message = "Submitted answers must not be empty")
    private List<String> submittedAnswers;
}
