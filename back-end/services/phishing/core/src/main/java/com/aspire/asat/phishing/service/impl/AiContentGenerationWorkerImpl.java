package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.AiGenerationJobStatus;
import com.aspire.asat.phishing.dto.enums.EmailTemplateStatus;
import com.aspire.asat.phishing.dto.enums.LandingPageStatus;
import com.aspire.asat.phishing.dto.request.AILandingPageRequest;
import com.aspire.asat.phishing.dto.request.AITemplateGenerateRequest;
import com.aspire.asat.phishing.dto.response.SanitizeHtmlResult;
import com.aspire.asat.phishing.dto.sqs.AiContentGenerationMessage;
import com.aspire.asat.phishing.model.AiGenerationJob;
import com.aspire.asat.phishing.model.EmailTemplate;
import com.aspire.asat.phishing.model.LandingPage;
import com.aspire.asat.phishing.repository.AiGenerationJobRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.service.AIContentGeneratorService;
import com.aspire.asat.phishing.service.AILandingPageService;
import com.aspire.asat.phishing.service.AiContentGenerationWorker;
import com.aspire.asat.phishing.service.HtmlSanitizerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.Optional;

/**
 * Default {@link AiContentGenerationWorker} implementation.
 *
 * <p>Loads the {@code AiGenerationJob} pointed to by the SQS message and routes execution
 * to the appropriate AI vendor call. Failures within a single dequeue are retried up to
 * {@code ai.generation.worker.processing-max-attempts} times (default 2) before the worker
 * persists FAILED on the job and stub. The worker never rethrows, allowing the listener to
 * delete the SQS message afterward.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiContentGenerationWorkerImpl implements AiContentGenerationWorker {

    private final ObjectMapper objectMapper;
    private final AiGenerationJobRepository aiGenerationJobRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final LandingPageRepository landingPageRepository;
    private final AIContentGeneratorService aiContentGeneratorService;
    private final AILandingPageService aiLandingPageService;
    private final HtmlSanitizerService htmlSanitizerService;

    @Value("${ai.generation.worker.processing-max-attempts:2}")
    private int processingMaxAttempts;

    @Value("${ai.generation.worker.processing-retry-backoff-ms:500}")
    private long processingRetryBackoffMs;

    @Override
    public void process(Message message) {
        AiContentGenerationMessage payload;
        try {
            payload = objectMapper.readValue(message.body(), AiContentGenerationMessage.class);
        } catch (Exception e) {
            log.error("Invalid AI content generation SQS message body, messageId={}: {}",
                    message.messageId(), e.getMessage(), e);
            return;
        }

        if (payload.getJobId() == null || payload.getJobType() == null) {
            log.error("AI content generation SQS message missing jobId/jobType: {}", payload);
            return;
        }

        Optional<AiGenerationJob> jobOpt = aiGenerationJobRepository.findById(payload.getJobId());
        if (jobOpt.isEmpty()) {
            log.error("AI content generation jobId={} not found in MongoDB; dropping message {}",
                    payload.getJobId(), message.messageId());
            return;
        }

        AiGenerationJob job = jobOpt.get();
        if (job.getStatus() == AiGenerationJobStatus.COMPLETED) {
            log.info("AI content generation jobId={} already COMPLETED; skipping duplicate message",
                    job.getId());
            return;
        }

        int maxAttempts = Math.max(1, processingMaxAttempts);
        Exception lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            job.setStatus(AiGenerationJobStatus.PROCESSING);
            job.setAttempts(job.getAttempts() + 1);
            job.setErrorMessage(null);
            aiGenerationJobRepository.save(job);

            try {
                switch (job.getJobType()) {
                    case EMAIL_TEMPLATE -> processEmailTemplate(job);
                    case LANDING_PAGE -> processLandingPage(job);
                    default -> throw new IllegalStateException("Unsupported jobType=" + job.getJobType());
                }
                return;
            } catch (Exception e) {
                lastFailure = e;
                log.warn(
                        "AI generation attempt {}/{} failed for jobId={} jobType={}: {}",
                        attempt,
                        maxAttempts,
                        job.getId(),
                        job.getJobType(),
                        e.getMessage(),
                        e);
                if (attempt < maxAttempts) {
                    backoffBeforeRetry(job.getId());
                }
            }
        }

        if (lastFailure != null) {
            handleFailure(job, lastFailure);
        }
    }

    private void backoffBeforeRetry(String jobId) {
        long delayMs = Math.max(0L, processingRetryBackoffMs);
        if (delayMs <= 0L) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("AI generation retry backoff interrupted for jobId={}", jobId, ie);
        }
    }

    private void processEmailTemplate(AiGenerationJob job) {
        AITemplateGenerateRequest request = job.getEmailTemplateRequest();
        if (request == null) {
            throw new IllegalStateException(
                    "Email template AI job " + job.getId() + " has no emailTemplateRequest payload");
        }

        EmailTemplate template = emailTemplateRepository.findById(job.getTargetEntityId())
                .orElseThrow(() -> new IllegalStateException(
                        "Stub email template " + job.getTargetEntityId() + " not found for jobId=" + job.getId()));

        AIContentGeneratorService.AIGeneratedContent generated =
                aiContentGeneratorService.generateTemplateContent(request, job.getClientId());

        if (!generated.success()) {
            throw new IllegalStateException("AI content generation failed: " + generated.errorMessage());
        }

        SanitizeHtmlResult sanitizeResult =
                htmlSanitizerService.sanitizeWithDetails(generated.htmlBody());

        template.setEmailBody(sanitizeResult.getSanitizedHtml());
        template.setEmailBodyText(generated.textBody());
        template.setStatus(EmailTemplateStatus.ACTIVE);
        template.setAiErrorMessage(null);
        emailTemplateRepository.save(template);

        job.setStatus(AiGenerationJobStatus.COMPLETED);
        job.setErrorMessage(null);
        aiGenerationJobRepository.save(job);

        log.info("AI email template jobId={} templateId={} completed", job.getId(), template.getId());
    }

    private void processLandingPage(AiGenerationJob job) {
        AILandingPageRequest request = job.getLandingPageRequest();
        if (request == null) {
            throw new IllegalStateException(
                    "Landing page AI job " + job.getId() + " has no landingPageRequest payload");
        }

        LandingPage page = landingPageRepository.findById(job.getTargetEntityId())
                .orElseThrow(() -> new IllegalStateException(
                        "Stub landing page " + job.getTargetEntityId() + " not found for jobId=" + job.getId()));

        String sanitizedHtml = aiLandingPageService.generateLandingPageHtml(request, job.getClientId());

        page.setHtmlContent(sanitizedHtml);
        page.setStatus(LandingPageStatus.ACTIVE);
        page.setAiErrorMessage(null);
        landingPageRepository.save(page);

        job.setStatus(AiGenerationJobStatus.COMPLETED);
        job.setErrorMessage(null);
        aiGenerationJobRepository.save(job);

        log.info("AI landing page jobId={} pageId={} completed", job.getId(), page.getId());
    }

    private void handleFailure(AiGenerationJob job, Exception e) {
        String reason = rootCauseMessage(e);
        log.error("AI content generation failed for jobId={} jobType={}: {}",
                job.getId(), job.getJobType(), reason, e);

        try {
            job.setStatus(AiGenerationJobStatus.FAILED);
            job.setErrorMessage(reason);
            aiGenerationJobRepository.save(job);
        } catch (Exception persistError) {
            log.error("Failed to persist FAILED state on jobId={}", job.getId(), persistError);
        }

        try {
            if (job.getTargetEntityId() == null) {
                return;
            }
            switch (job.getJobType()) {
                case EMAIL_TEMPLATE -> emailTemplateRepository.findById(job.getTargetEntityId())
                        .ifPresent(template -> {
                            template.setStatus(EmailTemplateStatus.FAILED);
                            template.setAiErrorMessage(reason);
                            emailTemplateRepository.save(template);
                        });
                case LANDING_PAGE -> landingPageRepository.findById(job.getTargetEntityId())
                        .ifPresent(page -> {
                            page.setStatus(LandingPageStatus.FAILED);
                            page.setAiErrorMessage(reason);
                            landingPageRepository.save(page);
                        });
                default -> { /* no-op */ }
            }
        } catch (Exception persistError) {
            log.error("Failed to persist FAILED state on stub document for jobId={}",
                    job.getId(), persistError);
        }
    }

    private String rootCauseMessage(Throwable t) {
        Throwable cursor = t;
        while (cursor.getCause() != null && cursor.getCause() != cursor) {
            cursor = cursor.getCause();
        }
        String msg = cursor.getMessage();
        return (msg == null || msg.isBlank()) ? cursor.getClass().getSimpleName() : msg;
    }
}
