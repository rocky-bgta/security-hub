package com.aspire.asat.phishing.dto.enums;

/**
 * How a vishing target is expected to respond during the scripted call.
 *
 * <ul>
 *   <li>{@code DTMF} - keypad digits only (e.g. "enter your PIN").</li>
 *   <li>{@code SPEECH} - spoken response only.</li>
 *   <li>{@code BOTH} - accept either DTMF or speech (default).</li>
 * </ul>
 */
public enum VishingInteractionMode {
    DTMF,
    SPEECH,
    BOTH
}
