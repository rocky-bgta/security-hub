package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.STTController;
import com.aspire.asat.phishing.dto.response.SttStatusResponse;
import com.aspire.asat.phishing.dto.response.SttUploadResponse;
import com.aspire.asat.phishing.service.SttTranscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class STTControllerImpl implements STTController {

    private final SttTranscriptionService sttTranscriptionService;

    @Override
    public ResponseEntity<SttUploadResponse> uploadAudio(MultipartFile audioFile, String languageHint) {
        SttUploadResponse response = sttTranscriptionService.handleUpload(audioFile, languageHint);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SttStatusResponse> getStatus(UUID audioId) {
        return ResponseEntity.ok(sttTranscriptionService.getStatus(audioId));
    }
}
