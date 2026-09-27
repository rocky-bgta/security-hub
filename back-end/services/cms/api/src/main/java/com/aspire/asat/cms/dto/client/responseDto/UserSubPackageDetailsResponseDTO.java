package com.aspire.asat.cms.dto.client.responseDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSubPackageDetailsResponseDTO {

    @NotBlank
    private String id;

    @NotBlank
    private String userId;

    @NotBlank
    private String subPackageId;

    @NotBlank
    private String subPackageName;

    @NotBlank
    private String status; // IN_PROGRESS, NOT_STARTED, COMPLETED, EXAM

    private String validity; // Optional: example "365 Days"

    private LocalDate assignedDate;  // when subpackage was assigned

    private LocalDate expiryDate;    // explicit expiry date

    private List<String> completedTopicIds;

    private String certificateLink;        // PDF certificate link

    private String imageCertificateLink;   // Image certificate link

    @NotNull
    private Double progress;               // overall subpackage progress in percentage

    private Instant lastSynced;            // last sync timestamp for progress update

    // Exam-related fields
    private Boolean examCompleted;       // Has the user submitted the exam?
    private Double examScore;            // Final exam % score
    private Boolean examPassed;          // Passed based on threshold?
    private Integer correctAnswers;      // Total correct
    private Integer incorrectAnswers;    // Total incorrect
    private Instant examCompletedAt;     // Timestamp when exam completed
    private Integer examAttempts;        // Number of times user attempted
}
