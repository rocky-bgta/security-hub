package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for voice-to-text transcription result.
 * Based on BRD Use Case 2.1.3.2: Multi-language voice input support
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptionResult {
    
    /**
     * The transcribed text content
     */
    private String transcribedText;
    
    /**
     * Language detected from the audio (ISO 639-1 code)
     */
    private String detectedLanguage;
    
    /**
     * Language name for display
     */
    private String detectedLanguageName;
    
    /**
     * Confidence score of the transcription (0.0 - 1.0)
     * BR-08: Confidence below 50% (0.5) should trigger warning
     */
    private double confidenceScore;
    
    /**
     * Whether the language was auto-detected
     */
    private boolean isAutoDetected;
    
    /**
     * Warning message if confidence is low
     */
    private String warningMessage;
    
    /**
     * Check if transcription confidence is low
     * @return true if confidence < 0.5 (50%)
     */
    public boolean isLowConfidence() {
        return confidenceScore < 0.5;
    }
}
