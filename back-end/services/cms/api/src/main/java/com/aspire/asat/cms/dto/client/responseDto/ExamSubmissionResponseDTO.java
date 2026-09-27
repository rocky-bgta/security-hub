package com.aspire.asat.cms.dto.client.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionResponseDTO {
    private String packageId;
    private String packageName;
    private Instant examCompletedAt;
    private int totalQuestions;
    private int correctAnswers;
    private int incorrectAnswers;
    private double percentageScore;
    private String status;
    private String certificateLink;
    private Integer passingScore;
}
