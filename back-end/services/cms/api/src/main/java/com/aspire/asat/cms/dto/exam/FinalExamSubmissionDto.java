package com.aspire.asat.cms.dto.exam;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalExamSubmissionDto {

    @NotBlank(message = "Exam ID must not be blank")
    private String examId;

    @NotBlank(message = "User ID must not be blank")
    private String userId;
}
