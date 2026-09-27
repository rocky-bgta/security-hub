package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Twilio-facing voice webhooks for the scripted vishing simulation. These are
 * form-encoded ({@code application/x-www-form-urlencoded}) callbacks secured by
 * the {@code X-Twilio-Signature} header plus the unguessable {@code trackingId};
 * they carry no JWT and are whitelisted at the gateway.
 */
@Tag(name = "Twilio Voice Webhooks", description = "Twilio callbacks for scripted vishing calls")
@RequestMapping(value = WebApiUrlConstants.VOICE_BASE_PATH)
public interface TwilioVoiceWebhookController {

    @Operation(summary = "Serve playback + gather TwiML for a call")
    @RequestMapping(value = WebApiUrlConstants.VOICE_CALL_TWIML,
            method = {RequestMethod.GET, RequestMethod.POST},
            produces = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity<String> callTwiml(
            @PathVariable String trackingId,
            @RequestHeader(value = "X-Twilio-Signature", required = false) String signature,
            @RequestParam(required = false) Map<String, String> params);

    @Operation(summary = "Handle captured DTMF/speech response")
    @PostMapping(value = WebApiUrlConstants.VOICE_CALL_GATHER,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity<String> gather(
            @PathVariable String trackingId,
            @RequestHeader(value = "X-Twilio-Signature", required = false) String signature,
            @RequestParam(required = false) Map<String, String> params);

    @Operation(summary = "Handle Twilio call-status callback")
    @PostMapping(value = WebApiUrlConstants.VOICE_CALL_TWILIO_STATUS,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<Void> twilioStatus(
            @PathVariable String trackingId,
            @RequestHeader(value = "X-Twilio-Signature", required = false) String signature,
            @RequestParam(required = false) Map<String, String> params);
}
