package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.AiGenerationJobStatus;
import com.aspire.asat.phishing.dto.enums.AiGenerationJobType;
import com.aspire.asat.phishing.dto.request.AILandingPageRequest;
import com.aspire.asat.phishing.dto.request.AITemplateGenerateRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Bookkeeping document for an asynchronous AI content-generation job.
 *
 * <p>Persists the full request payload (so the SQS listener can rebuild the call without
 * re-reading the original HTTP body), the lifecycle status, and any error message captured
 * from a failed run. The {@code targetEntityId} links the job to the stub
 * {@code email_templates} or {@code landing_pages} document that was created up front.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "ai_content_generation_jobs")
public class AiGenerationJob {

    @Id
    private String id;

    @Indexed
    private AiGenerationJobType jobType;

    @Indexed
    private AiGenerationJobStatus status;

    @Indexed
    private String clientId;

    private String createdBy;

    private String createdByRole;

    /**
     * Identifier of the stub email template / landing page document this job will populate.
     */
    @Indexed
    private String targetEntityId;

    /**
     * Full email template generation payload. Populated only when {@code jobType == EMAIL_TEMPLATE}.
     */
    private AITemplateGenerateRequest emailTemplateRequest;

    /**
     * Full landing page generation payload. Populated only when {@code jobType == LANDING_PAGE}.
     */
    private AILandingPageRequest landingPageRequest;

    /**
     * Root-cause message from the most recent failed attempt; null on success or while processing.
     */
    private String errorMessage;

    @Builder.Default
    private int attempts = 0;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
