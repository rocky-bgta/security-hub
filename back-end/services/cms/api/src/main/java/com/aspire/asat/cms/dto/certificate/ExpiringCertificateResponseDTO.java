package com.aspire.asat.cms.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for certificates expiring within the next 30 days.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpiringCertificateResponseDTO {
    private String learnerName; // fullName from UserCertificate
    private String clientAdminId;
    private String clientAdminName;
    private String courseName; // productName from UserCertificate
    private String certificateId;
    private Instant expiryDate;
    private String status; // "Expiring"
}

