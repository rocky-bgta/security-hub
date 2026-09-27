package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.TwilioVoiceWebhookController;
import com.aspire.asat.phishing.service.VishingWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TwilioVoiceWebhookControllerImpl implements TwilioVoiceWebhookController {

    private final VishingWebhookService vishingWebhookService;

    @Override
    public ResponseEntity<String> callTwiml(String trackingId, String signature, Map<String, String> params) {
        String twiml = vishingWebhookService.buildCallTwiml(trackingId, safeParams(params), signature);
        return xml(twiml);
    }

    @Override
    public ResponseEntity<String> gather(String trackingId, String signature, Map<String, String> params) {
        String twiml = vishingWebhookService.handleGather(trackingId, safeParams(params), signature);
        return xml(twiml);
    }

    @Override
    public ResponseEntity<Void> twilioStatus(String trackingId, String signature, Map<String, String> params) {
        vishingWebhookService.handleStatusCallback(trackingId, safeParams(params), signature);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<String> xml(String body) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(body);
    }

    private Map<String, String> safeParams(Map<String, String> params) {
        return params != null ? params : new HashMap<>();
    }
}
