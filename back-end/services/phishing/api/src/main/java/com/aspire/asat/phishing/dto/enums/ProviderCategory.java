package com.aspire.asat.phishing.dto.enums;

/**
 * Capability grouping for a third-party deepfake provider.
 *
 * <p>A single {@link #isDefault default} provider is maintained per client per
 * category, since voice-cloning and video-rendering providers are not
 * interchangeable.
 */
public enum ProviderCategory {

    /** Voice cloning + speech synthesis (e.g. ElevenLabs, Fish Audio). */
    VOICE_CLONING,

    /** Deepfake video rendering (e.g. HeyGen). */
    VIDEO_RENDERING
}
