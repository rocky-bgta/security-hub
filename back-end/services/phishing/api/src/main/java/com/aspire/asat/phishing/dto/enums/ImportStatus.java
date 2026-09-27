package com.aspire.asat.phishing.dto.enums;

/**
 * Result status for landing-page import operation.
 */
public enum ImportStatus {
    SUCCESS_FULL,
    SUCCESS_PARTIAL,
    BLOCKED_WAF,
    BLOCKED_GEO_OR_BOT,
    UNSUPPORTED_DYNAMIC_FLOW,
    FAILED
}

