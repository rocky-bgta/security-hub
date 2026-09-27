package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.response.TranscriptionResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Service interface for voice-to-text transcription.
 * Based on BRD Use Case 2.1.3.2: Multi-language voice input support
 */
public interface VoiceTranscriptionService {
    
    /**
     * Transcribe audio file to text
     * @param audioFile The audio file to transcribe
     * @param languageHint Optional language hint (ISO 639-1 code)
     * @return TranscriptionResult with text and confidence
     */
    TranscriptionResult transcribe(MultipartFile audioFile, String languageHint);
    
    /**
     * Get supported languages for voice input
     * @return Map of language code to language name
     */
    Map<String, String> getSupportedLanguages();
    
    /**
     * Check if a language is supported
     * @param languageCode ISO 639-1 language code
     * @return true if supported
     */
    boolean isLanguageSupported(String languageCode);
}
