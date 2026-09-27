package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.sqs.DeepfakeRenderMessage;

public interface DeepfakeSqsService {

    void sendMessage(DeepfakeRenderMessage message);
}
