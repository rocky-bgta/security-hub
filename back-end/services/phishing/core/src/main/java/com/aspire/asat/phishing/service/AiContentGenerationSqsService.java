package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.sqs.AiContentGenerationMessage;

/**
 * Publisher abstraction for the shared {@code ai-content-generation-queue}.
 */
public interface AiContentGenerationSqsService {

    void sendMessage(AiContentGenerationMessage message);
}
