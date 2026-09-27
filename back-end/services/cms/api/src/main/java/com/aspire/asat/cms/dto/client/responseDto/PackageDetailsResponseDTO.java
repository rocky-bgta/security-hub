package com.aspire.asat.cms.dto.client.responseDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageDetailsResponseDTO {
    private String packageId;
    private String packageName;
    private String validity;
    private String expireDate;
    private String status;
    private double progress;

    private boolean examCompleted;
    private double examScore;
    private boolean examPassed;
    private int correctAnswers;
    private int incorrectAnswers;
    private Instant examCompletedAt;
    private int examAttempts;

    private List<CompletedCourseDTO> completedCourses;  // ✅ New field

    private PackageExamDTO exam;
    private String certificateLink;
    private String ImageCertificateLink;
}
