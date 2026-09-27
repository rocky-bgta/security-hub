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
public class ExamSubmissionRequestDTO {
    @NotBlank(message = "User ID must not be blank")
    private String userId;         // ✅ Added userId

    @NotBlank(message = "Package ID must not be blank")
    private String packageId;

    @NotBlank(message = "Exam ID must not be blank")
    private String examId;

    @NotEmpty(message = "Answers list must not be empty")
    private List<QuestionAnswerDTO> answers;
}
