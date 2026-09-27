package com.aspire.asat.phishing.dto.enums;

/**
 * Vishing response stages from campaign setup (maps to training trigger policy).
 * CALL_ENGAGED = user stayed engaged (pressed IVR / followed minor instruction);
 * COMPROMISED = user reveals sensitive information.
 */
public enum VoiceResponseStage {
    CALL_ENGAGED,
    COMPROMISED
}
