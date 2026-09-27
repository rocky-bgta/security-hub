package com.aspire.asat.phishing.dto.enums;

/**
 * Distinguishes how a {@code DeepfakeVoiceClone} was created so list endpoints
 * can return only the relevant set (vishing campaign voice-setup vs deepfake video wizard).
 */
public enum VoiceCloneSource {
    VISHING,
    DEEPFAKE_VIDEO
}
