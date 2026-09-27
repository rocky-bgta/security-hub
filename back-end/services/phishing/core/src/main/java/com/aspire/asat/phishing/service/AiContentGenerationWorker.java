package com.aspire.asat.phishing.service;

import software.amazon.awssdk.services.sqs.model.Message;

/**
 * Background processor for AI content generation jobs pulled from the
 * {@code ai-content-generation-queue}.
 */
public interface AiContentGenerationWorker {

    /**
     * Process a single SQS message: load the {@code AiGenerationJob} by id, run the AI
     * vendor call, and update both the job and the stub email-template / landing-page
     * document with COMPLETED or FAILED state.
     *
     * <p>The implementation is responsible for catching all exceptions and persisting them
     * to MongoDB; callers should not redeliver on failure.
     */
    void process(Message message);
}
