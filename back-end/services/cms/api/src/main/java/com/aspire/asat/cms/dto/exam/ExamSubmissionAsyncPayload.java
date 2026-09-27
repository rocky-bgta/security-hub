package com.aspire.asat.cms.dto.exam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for async processing after exam submission.
 * Contains all data needed for certificate generation, DB updates, and notification
 * (no request context available in async thread).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamSubmissionAsyncPayload {

    private String userId;
    private String examId;
    private String subPackageId;
    private boolean passed;

    /** For certificate PDF and UserCertificate */
    private String userEmail;
    private String userFullName;

    /** For notification email */
    private String adminEmail;
    private String adminName;
    private String clientAdminId;
    /** Course/product name for notification (e.g. subPackage name) */
    private String courseTitle;
}
