package com.aspire.asat.phishing.dto.enums;

/**
 * Supported video rendering engines for deepfake video generation.
 * HEYGEN is wired to a real API; the remaining self-hosted engines are
 * served by a stub adapter until dedicated ML infrastructure is available.
 */
public enum VideoRenderProvider {
    HEYGEN,
}
