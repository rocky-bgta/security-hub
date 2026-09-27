package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.sqs.SttTranscriptionMessage;

public interface SttSqsService {
    void sendMessage(SttTranscriptionMessage message);
}
