package com.aspire.asat.phishing.sms.impl;

import com.aspire.asat.common.util.PhoneNumberUtils;
import com.aspire.asat.phishing.dto.enums.SmsProviderType;
import com.aspire.asat.phishing.sms.SmsProvider;
import com.aspire.asat.phishing.sms.SmsSendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class GenericRestSmsProvider implements SmsProvider {

    private static final Set<String> SECRET_BODY_FIELDS = Set.of("api_key", "api_secret");

    private final String apiKey;
    private final String apiSecret;
    private final String senderId;
    private final String baseUrl;
    private final Map<String, String> metadata;
    private final WebClient webClient;

    public GenericRestSmsProvider(String apiKey, String apiSecret, String senderId,
                                  String baseUrl, Map<String, String> metadata) {
        this(apiKey, apiSecret, senderId, baseUrl, metadata, WebClient.builder().build());
    }

    GenericRestSmsProvider(String apiKey, String apiSecret, String senderId,
                           String baseUrl, Map<String, String> metadata, WebClient webClient) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.senderId = senderId;
        this.baseUrl = baseUrl;
        this.metadata = metadata != null ? metadata : Map.of();
        this.webClient = webClient;
    }

    @Override
    public SmsSendResult send(String mobileNumber, String message) {
        try {
            String to = PhoneNumberUtils.normalize(mobileNumber);
            Map<String, Object> body = genericBody(to, message);

            String url = resolveUrl();
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            String authHeader = metadata.getOrDefault("authHeader", "Authorization");
            String authValue = metadata.getOrDefault("authPrefix", "Bearer ") + apiKey;
            headers.put(authHeader, authValue);
            logOutgoingRequest(url, headers, body);

            RequestBodySpec spec = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(authHeader, authValue);

            String response = spec.bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return SmsSendResult.ok(response != null ? response : "generic-rest-sent");
        } catch (WebClientResponseException e) {
            String apiBody = e.getResponseBodyAsString();
            log.error("Generic REST SMS send failed: status={} url={} body={}",
                    e.getStatusCode(), resolveUrl(), apiBody, e);
            return SmsSendResult.fail("GENERIC_REST_ERROR",
                    e.getStatusCode() + " " + (StringUtils.hasText(apiBody) ? apiBody : e.getMessage()));
        } catch (Exception e) {
            log.error("Generic REST SMS send failed: {}", e.getMessage(), e);
            return SmsSendResult.fail("GENERIC_REST_ERROR", e.getMessage());
        }
    }

    @Override
    public SmsProviderType getProviderType() {
        return SmsProviderType.GENERIC_REST;
    }

    /**
     * POSTs to {@code baseUrl} as-is unless {@code sendPath} is explicitly set in metadata.
     */
    String resolveUrl() {
        String sendPath = metadata.get("sendPath");
        if (sendPath == null || sendPath.isBlank() || baseUrl == null) {
            return baseUrl;
        }
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = sendPath.startsWith("/") ? sendPath : "/" + sendPath;
        return base + path;
    }

    private Map<String, Object> genericBody(String to, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put(metadata.getOrDefault("phoneField", "to"), to);
        body.put(metadata.getOrDefault("messageField", "message"), message);
        body.put(metadata.getOrDefault("senderField", "sender"), senderId);
        body.put(metadata.getOrDefault("apiKeyField", "api_key"), apiKey);
        if (apiSecret != null && !apiSecret.isBlank()) {
            body.put(metadata.getOrDefault("apiSecretField", "api_secret"), apiSecret);
        }
        return body;
    }

    private void logOutgoingRequest(String url, Map<String, String> headers, Map<String, Object> body) {
        log.info("Generic REST SMS outgoing request: POST {} headers={} body={}",
                url, redactHeaders(headers), redactBody(body));
    }

    private static Map<String, String> redactHeaders(Map<String, String> headers) {
        Map<String, String> redacted = new LinkedHashMap<>();
        headers.forEach((name, value) -> {
            if ("Authorization".equalsIgnoreCase(name) || "X-Api-Key".equalsIgnoreCase(name)) {
                redacted.put(name, redactSecret(value));
            } else {
                redacted.put(name, value);
            }
        });
        return redacted;
    }

    private static Map<String, Object> redactBody(Map<String, Object> body) {
        Map<String, Object> redacted = new LinkedHashMap<>();
        body.forEach((key, value) -> {
            if (SECRET_BODY_FIELDS.contains(key) || "apiKey".equals(key) || "apiSecret".equals(key)) {
                redacted.put(key, value instanceof String s ? redactSecret(s) : "***");
            } else {
                redacted.put(key, value);
            }
        });
        return redacted;
    }

    private static String redactSecret(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        int keep = Math.min(4, value.length());
        return "***" + value.substring(value.length() - keep);
    }
}
