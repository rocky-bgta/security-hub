package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.constant.SupportedLanguages;
import com.aspire.asat.phishing.dto.response.TranscriptionResult;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.VoiceTranscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Implementation of VoiceTranscriptionService.
 * Provides voice-to-text transcription for email template creation.
 * Based on BRD Use Case 2.1.3.2: Multi-language voice input support
 * 
 * TODO: Integrate with actual speech-to-text service (Google Cloud Speech, AWS Transcribe, Azure Speech)
 * This implementation provides a placeholder.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VoiceTranscriptionServiceImpl implements VoiceTranscriptionService {
    
    // TODO: Inject actual transcription client when available
    // private final SpeechToTextClient speechClient;
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] SUPPORTED_AUDIO_TYPES = {
            "audio/wav", "audio/mpeg", "audio/mp3", "audio/ogg", 
            "audio/webm", "audio/m4a", "audio/x-m4a"
    };
    
    @Override
    public TranscriptionResult transcribe(MultipartFile audioFile, String languageHint) {
        log.info("Transcribing audio file: {}, size: {}, language hint: {}",
                audioFile.getOriginalFilename(), audioFile.getSize(), languageHint);
        
        // Validate file
        validateAudioFile(audioFile);
        
        // Validate language if provided
        if (languageHint != null && !languageHint.isBlank() && !isLanguageSupported(languageHint)) {
            throw new ServiceException("Language not supported for voice input: " + languageHint);
        }
        
        try {
            // TODO: Replace with actual speech-to-text API call
            // For now, return a placeholder result
            
            String detectedLanguage = languageHint != null && !languageHint.isBlank() 
                    ? languageHint 
                    : "en";
            String languageName = SupportedLanguages.getLanguageName(detectedLanguage);
            
            // Mock transcription result
            TranscriptionResult result = TranscriptionResult.builder()
                    .transcribedText("[Voice transcription placeholder - integrate with speech-to-text service]")
                    .detectedLanguage(detectedLanguage)
                    .detectedLanguageName(languageName != null ? languageName : "Unknown")
                    .confidenceScore(0.85) // Mock confidence
                    .isAutoDetected(languageHint == null || languageHint.isBlank())
                    .warningMessage(null)
                    .build();
            
            // Check confidence and set warning if needed
            if (result.isLowConfidence()) {
                result.setWarningMessage(SupportedLanguages.LOW_CONFIDENCE_WARNING);
            }
            
            log.info("Transcription completed: language={}, confidence={}",
                    result.getDetectedLanguage(), result.getConfidenceScore());
            
            return result;
            
        } catch (Exception e) {
            log.error("Voice transcription failed: {}", e.getMessage(), e);
            throw new ServiceException("Voice transcription failed. Please try again.");
        }
    }
    
    @Override
    public Map<String, String> getSupportedLanguages() {
        return SupportedLanguages.VOICE_INPUT_LANGUAGES;
    }
    
    @Override
    public boolean isLanguageSupported(String languageCode) {
        return SupportedLanguages.isSupported(languageCode);
    }
    
    /**
     * Validate the uploaded audio file
     */
    private void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("Audio file is required");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ServiceException("Audio file size exceeds maximum allowed size of 10MB");
        }
        
        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String supportedType : SUPPORTED_AUDIO_TYPES) {
            if (supportedType.equalsIgnoreCase(contentType)) {
                isValidType = true;
                break;
            }
        }
        
        if (!isValidType) {
            throw new ServiceException("Invalid audio file type. Supported: WAV, MP3, OGG, WebM, M4A");
        }
    }
}
