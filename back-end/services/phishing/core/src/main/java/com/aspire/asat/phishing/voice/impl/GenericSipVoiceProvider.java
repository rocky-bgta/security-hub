package com.aspire.asat.phishing.voice.impl;

import com.aspire.asat.common.util.PhoneNumberUtils;
import com.aspire.asat.phishing.dto.enums.VoiceProviderType;
import com.aspire.asat.phishing.voice.VoiceCallRequest;
import com.aspire.asat.phishing.voice.VoiceCallResult;
import com.aspire.asat.phishing.voice.VoiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GenericSipVoiceProvider implements VoiceProvider {

    private final String apiKey;
    private final String apiSecret;
    private final String callerId;
    private final String baseUrl;
    private final Map<String, String> metadata;
    private final WebClient webClient;

    public GenericSipVoiceProvider(String apiKey, String apiSecret, String callerId,
                                   String baseUrl, Map<String, String> metadata) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.callerId = callerId;
        this.baseUrl = baseUrl;
        this.metadata = metadata != null ? metadata : Map.of();
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public VoiceCallResult initiateCall(VoiceCallRequest request) {
        try {
            String to = PhoneNumberUtils.normalize(request.toPhone());
            Map<String, Object> body = new HashMap<>();
            body.put(metadata.getOrDefault("phoneField", "to"), to);
            body.put(metadata.getOrDefault("scriptField", "script"), request.scriptBody());
            body.put(metadata.getOrDefault("callerField", "caller_id"),
                    callerId != null ? callerId : request.callerId());
            if (request.externalVoiceId() != null) {
                body.put(metadata.getOrDefault("voiceIdField", "voice_id"), request.externalVoiceId());
            }
            if (request.trackingId() != null) {
                body.put(metadata.getOrDefault("trackingField", "tracking_id"), request.trackingId());
            }
            body.put(metadata.getOrDefault("apiKeyField", "api_key"), apiKey);
            if (apiSecret != null && !apiSecret.isBlank()) {
                body.put(metadata.getOrDefault("apiSecretField", "api_secret"), apiSecret);
            }

            String response = webClient.post()
                    .uri(metadata.getOrDefault("callPath", "/call"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(metadata.getOrDefault("authHeader", "Authorization"),
                            metadata.getOrDefault("authPrefix", "Bearer ") + apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return VoiceCallResult.ok(response != null ? response : "generic-sip-call");
        } catch (Exception e) {
            log.error("Generic SIP voice call failed: {}", e.getMessage(), e);
            return VoiceCallResult.fail(e.getMessage());
        }
    }

    @Override
    public VoiceProviderType getProviderType() {
        return VoiceProviderType.GENERIC_SIP;
    }
}
