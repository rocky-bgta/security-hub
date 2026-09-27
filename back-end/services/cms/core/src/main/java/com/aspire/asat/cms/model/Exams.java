package com.aspire.asat.cms.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "exams")
public class Exams {

    @Id
    private String examId;

    @Indexed
    private String userId;
    private String fullName;
    private String productName;

    private String clientAdminId;

    @Indexed
    private String subPackageId;

    private String title;

    private double passingScore;
    private Integer totalQuestions;

    private String examDetails;

    private Instant createdAt;

    // Exam Results (moved from UserSubPackage)
    private boolean examCompleted;       // Has the user submitted the exam?
    private double examScore;            // Final exam % score
    private boolean examPassed;          // Passed based on threshold?
    private int correctAnswers;          // Total correct
    private int incorrectAnswers;        // Total incorrect
    private Instant examCompletedAt;     // Timestamp when exam completed
    private int examAttempts;            // Number of attempts (for retakes)

    // Certificate links (for quick access)
    private String certificateLink;      // PDF certificate link
    private String imageCertificateLink; // Image certificate link
    private String countryId;
    private String mspId;
}
