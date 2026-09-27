package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.MicroContentCreateRequest;
import com.aspire.asat.phishing.dto.response.MicroContentJobResponse;
import com.aspire.asat.phishing.dto.response.MicroContentStatusResponse;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.UUID;

/**
 * Orchestrates creation of "microContent" CMS material from deepfake videos asynchronously via SQS.
 * Each deepfake video becomes its own CMS topic (topic + chapter + VIDEO content).
 * Removal is synchronous (no SQS) and calls CMS deleteTopicById.
 */
public interface MicroContentService {

    /**
     * Persists a job and enqueues it for async processing; returns immediately.
     */
    MicroContentJobResponse enqueue(MicroContentCreateRequest request);

    /**
     * Processes a queued job (called by the SQS listener). Returns {@code true} when the message
     * should be deleted (done or duplicate of a completed job), {@code false} to defer.
     */
    boolean processJob(Message message);

    /**
     * Returns the current status of a job for the calling client.
     */
    MicroContentStatusResponse getStatus(UUID jobId);

    /**
     * Synchronously removes the deepfake video from micro content by deleting its CMS topic.
     * Does not use SQS.
     */
    void removeByVideoId(UUID videoId);
}
