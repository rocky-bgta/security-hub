package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.dto.response.SttStatusResponse;
import com.aspire.asat.phishing.dto.response.SttUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RequestMapping(value = "/api/v1/stt", produces = "application/json")
@Tag(name = "Speech-to-Text API", description = "Endpoints for uploading audio files and retrieving transcription status")
public interface STTController {

    @Operation(summary = "Upload audio file for transcription", description = "Accepts an audio file and an optional language hint, returns a unique audio ID for tracking transcription status")
    @PostMapping("/transcribe")
    ResponseEntity<SttUploadResponse> uploadAudio(
            @RequestPart("file") MultipartFile audioFile,
            @RequestParam(required = false) String languageHint
    );

    @Operation(summary = "Get transcription status", description = "Retrieves the transcription status and result for a given audio ID")
    @GetMapping("/{audioId}")
    ResponseEntity<SttStatusResponse> getStatus(@PathVariable UUID audioId);
}
