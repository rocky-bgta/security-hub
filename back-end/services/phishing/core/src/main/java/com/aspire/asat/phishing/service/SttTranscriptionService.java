package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.response.SttStatusResponse;
import com.aspire.asat.phishing.dto.response.SttUploadResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface SttTranscriptionService {
    SttUploadResponse handleUpload(MultipartFile audioFile, String languageHint);
    SttStatusResponse getStatus(UUID audioId);
    void processAudio(Message message);
}
