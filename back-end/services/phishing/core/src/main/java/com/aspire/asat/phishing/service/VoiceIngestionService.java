package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.VoiceCallResultRequest;
import com.aspire.asat.phishing.dto.request.VoiceCallStatusRequest;

public interface VoiceIngestionService {

    void recordCallStatus(String trackingId, VoiceCallStatusRequest request);

    void processCallResult(String trackingId, VoiceCallResultRequest request);
}
