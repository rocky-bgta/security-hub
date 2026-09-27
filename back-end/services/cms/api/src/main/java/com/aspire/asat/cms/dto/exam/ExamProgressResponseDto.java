package com.aspire.asat.cms.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamProgressResponseDto {

    private String examId;
    private String userId;
    private int totalQuestions;
    private int questionsAttempted;
    private int questionsLeft;
    private double submissionPercentage;
    private int correctAnswers;
    private int incorrectAnswers;
    private boolean examCompleted;
}
