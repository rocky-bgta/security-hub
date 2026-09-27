package com.aspire.asat.phishing.service;

import java.util.Map;

/**
 * Handles Twilio's per-call voice webhooks for the scripted vishing simulation:
 * serving playback/gather TwiML, interpreting the captured response, and
 * recording call lifecycle status. Each method validates Twilio's signature
 * before performing any work.
 */
public interface VishingWebhookService {

    /**
     * Builds the playback + gather TwiML served when Twilio fetches the call URL.
     */
    String buildCallTwiml(String trackingId, Map<String, String> params, String signature);

    /**
     * Interprets the captured DTMF/speech response, records the outcome, and
     * returns closing TwiML.
     */
    String handleGather(String trackingId, Map<String, String> params, String signature);

    /**
     * Maps a Twilio call-status callback to a recipient status and records it.
     */
    void handleStatusCallback(String trackingId, Map<String, String> params, String signature);
}
