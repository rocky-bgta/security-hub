package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.sqs.MicroContentMessage;

/**
 * Publishes micro content creation jobs to SQS for asynchronous processing.
 */
public interface MicroContentSqsService {

    void sendMessage(MicroContentMessage message);
}
