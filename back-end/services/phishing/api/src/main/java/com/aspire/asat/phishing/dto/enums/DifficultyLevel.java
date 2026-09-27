package com.aspire.asat.phishing.dto.enums;

/**
 * Difficulty level for phishing templates.
 * Indicates how sophisticated the phishing attempt is.
 */
public enum DifficultyLevel {
    /**
     * Beginner-level phishing simulation (easy to identify).
     */
    BEGINNER,

    /**
     * Intermediate phishing simulation (moderate sophistication).
     */
    INTERMEDIATE,

    /**
     * Advanced phishing simulation (hard to identify).
     */
    ADVANCED,

    /**
     * Spear phishing simulation (highly targeted and personalized).
     */
    SPEAR_PHISHING
}
