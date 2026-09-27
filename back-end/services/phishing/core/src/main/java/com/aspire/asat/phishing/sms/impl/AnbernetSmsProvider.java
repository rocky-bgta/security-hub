package com.aspire.asat.phishing.sms.impl;

import com.aspire.asat.common.util.PhoneNumberUtils;
import com.aspire.asat.phishing.dto.enums.SmsProviderType;
import com.aspire.asat.phishing.sms.SmsProvider;
import com.aspire.asat.phishing.sms.SmsSendResult;
import io.netty.channel.ChannelOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Anbernet (Bangladesh) SMS gateway. Maps SMS server config to the Anbernet JSON body:
 * {@code account} = config name, {@code api_key} = apiKey, {@code senderid} = senderId.
 */
@Slf4j
public class AnbernetSmsProvider implements SmsProvider {

    static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(2);

    private final String account;
    private final String apiKey;
    private final String senderId;
    private final String baseUrl;
    private final WebClient webClient;

    public AnbernetSmsProvider(String name, String apiKey, String senderId, String baseUrl,
                               Map<String, String> metadata) {
        this(name, apiKey, senderId, baseUrl, metadata, createWebClient());
    }

    AnbernetSmsProvider(String name, String apiKey, String senderId, String baseUrl,
                        Map<String, String> metadata, WebClient webClient) {
        this.account = resolveAccount(name, metadata);
        this.apiKey = apiKey;
        this.senderId = senderId;
        this.baseUrl = baseUrl;
        this.webClient = webClient;
    }

    @Override
    public SmsSendResult send(String mobileNumber, String message) {
        if (!StringUtils.hasText(baseUrl)) {
            return SmsSendResult.fail("ANBERNET_CONFIG_ERROR", "baseUrl is required");
        }
        if (!StringUtils.hasText(account)) {
            return SmsSendResult.fail("ANBERNET_CONFIG_ERROR",
                    "SMS server name is required as Anbernet account");
        }
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(senderId)) {
            return SmsSendResult.fail("ANBERNET_CONFIG_ERROR", "apiKey and senderId are required");
        }

        String to = PhoneNumberUtils.normalize(mobileNumber);
        if (to == null) {
            return SmsSendResult.fail("ANBERNET_CONFIG_ERROR", "Invalid mobile number");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("account", account);
        body.put("api_key", apiKey);
        body.put("senderid", senderId);
        body.put("receivers", List.of(to));
        body.put("msgdata", message);

        try {
            log.info("Anbernet SMS outgoing request: POST {} headers={{Content-Type=application/json}} body={}",
                    baseUrl, redactBody(body));
            String response = webClient.post()
                    .uri(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(REQUEST_TIMEOUT);

            if (isSuccessfulResponse(response)) {
                return SmsSendResult.ok(response);
            }
            log.warn("Anbernet SMS response indicated failure for {}: {}", to, response);
            return SmsSendResult.fail("ANBERNET_ERROR",
                    response != null ? response : "empty response");
        } catch (WebClientResponseException e) {
            String apiBody = e.getResponseBodyAsString();
            log.error("Anbernet SMS send failed: status={} url={} body={}",
                    e.getStatusCode(), baseUrl, apiBody, e);
            return SmsSendResult.fail("ANBERNET_ERROR",
                    e.getStatusCode() + " " + (StringUtils.hasText(apiBody) ? apiBody : e.getMessage()));
        } catch (Exception e) {
            log.error("Anbernet SMS send failed: {}", e.getMessage(), e);
            return SmsSendResult.fail("ANBERNET_ERROR", e.getMessage());
        }
    }

    @Override
    public SmsProviderType getProviderType() {
        return SmsProviderType.GENERIC_REST;
    }

    static WebClient createWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) REQUEST_TIMEOUT.toMillis())
                .responseTimeout(REQUEST_TIMEOUT);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    static String resolveAccount(String name, Map<String, String> metadata) {
        if (metadata != null && StringUtils.hasText(metadata.get("account"))) {
            return metadata.get("account");
        }
        return name;
    }

    private static Map<String, Object> redactBody(Map<String, Object> body) {
        Map<String, Object> redacted = new LinkedHashMap<>(body);
        Object apiKey = redacted.get("api_key");
        if (apiKey instanceof String value && StringUtils.hasText(value)) {
            int keep = Math.min(4, value.length());
            redacted.put("api_key", "***" + value.substring(value.length() - keep));
        }
        return redacted;
    }

    boolean isSuccessfulResponse(String response) {
        if (!StringUtils.hasText(response)) {
            return false;
        }
        String lower = response.toLowerCase();
        return lower.contains("\"status\":\"success\"")
                || lower.contains("\"status\": \"success\"")
                || (lower.contains("success_count") && !lower.contains("\"success_count\":0")
                    && !lower.contains("\"success_count\": 0"));
    }
}
