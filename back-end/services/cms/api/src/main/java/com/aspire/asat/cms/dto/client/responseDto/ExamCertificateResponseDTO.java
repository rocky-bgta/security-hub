package com.aspire.asat.cms.dto.client.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for exam certificates with joined data from UserSubPackage and UserCertificate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamCertificateResponseDTO {
    private Double examScore;
    private String examPassed;
    private String fullName;
    private Instant expiryDate;
    private Instant issueDate;
    private String status;
    private String certificateLink;
    private String productName;
    private String clientAdminId;
    private String certificateId;
    private String examId;
}

