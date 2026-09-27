package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.VoiceIngestionController;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.request.VoiceCallResultRequest;
import com.aspire.asat.phishing.dto.request.VoiceCallStatusRequest;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.service.VoiceIngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VoiceIngestionControllerImpl implements VoiceIngestionController {

    private final VoiceIngestionService voiceIngestionService;

    @Value("${voice.ingestion.api-key:}")
    private String configuredApiKey;

    @Override
    public ResponseEntity<ApiResponseDto<String>> recordCallStatus(
            String trackingId, String apiKey, VoiceCallStatusRequest request) {
        validateApiKey(apiKey);
        voiceIngestionService.recordCallStatus(trackingId, request);
        return ResponseEntity.ok(ApiResponseDto.<String>builder()
                .data(trackingId)
                .message("Call status recorded")
                .build());
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> processCallResult(
            String trackingId, String apiKey, VoiceCallResultRequest request) {
        validateApiKey(apiKey);
        voiceIngestionService.processCallResult(trackingId, request);
        return ResponseEntity.ok(ApiResponseDto.<String>builder()
                .data(trackingId)
                .message("Call result processed")
                .build());
    }

    private void validateApiKey(String apiKey) {
        if (!StringUtils.hasText(configuredApiKey)) {
            return;
        }
        if (!configuredApiKey.equals(apiKey)) {
            throw new PhishingValidationException("Invalid voice ingestion API key");
        }
    }
}
