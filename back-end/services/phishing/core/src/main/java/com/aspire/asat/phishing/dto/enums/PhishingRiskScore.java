package com.aspire.asat.phishing.dto.enums;

/**
 * Risk score points per email activity for phishing simulations.
 * Replaces the numeric constants previously defined in PhishingRiskScorePoints.
 */
public enum PhishingRiskScore {

    /** Did not open / Never clicked */
    NONE(0.0),
    /** Did not open email but reported */
    REPORTED_WITHOUT_OPEN(0.0),
    /** Opened the email but reported */
    OPENED_BUT_REPORTED(10.0),
    /** Opened the email (Scenario B: low-moderate risk) */
    OPENED(20.0),
    /** Clicked on link */
    CLICKED(75.0),
    /** Submitted information / data submitted */
    DATA_SUBMITTED(100.0),
    /** Vishing: call failed, missed, or reported without answering */
    VOICE_NO_ANSWER(0.0),
    /** Vishing: answered but reported the suspicious call */
    VOICE_ANSWERED_BUT_REPORTED(10.0),
    /** Vishing: answered, low risk, no compromise (aligned with email OPENED) */
    VOICE_ANSWERED(20.0),
    /** Vishing: stayed engaged, pressed IVR, followed minor instruction (aligned with email CLICKED) */
    VOICE_ENGAGED(75.0),
    /** Vishing: shared OTP/password/personal info, followed attacker instruction (aligned with email DATA_SUBMITTED) */
    VOICE_COMPROMISED(100.0);

    private final double points;

    PhishingRiskScore(double points) {
        this.points = points;
    }

    public double getPoints() {
        return points;
    }
}

