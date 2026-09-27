package com.aspire.asat.notification.service.sms.impl;

import com.aspire.asat.notification.service.sms.SmsProvider;
import com.aspire.asat.notification.util.PhoneNumberParser;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SMS provider for Bangladesh (+880) via Anbernet REST API.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BangladeshSmsProvider implements SmsProvider {

    private final PhoneNumberParser phoneNumberParser;
    private final WebClient webClient;

    @Value("${sms.providers.bangladesh.enabled:false}")
    private boolean enabled;

    @Value("${sms.providers.bangladesh.api-url:}")
    private String apiUrl;

    @Value("${sms.providers.bangladesh.account:}")
    private String account;

    @Value("${sms.providers.bangladesh.api-key:}")
    private String apiKey;

    @Value("${sms.providers.bangladesh.sender-id:}")
    private String senderId;

    @Value("${sms.providers.bangladesh.country-codes:+880}")
    private String countryCodes;

    private List<String> supportedCountryCodes;
    private boolean configured;

    @PostConstruct
    public void init() {
        configured = StringUtils.hasText(apiUrl)
                && StringUtils.hasText(account)
                && StringUtils.hasText(apiKey)
                && StringUtils.hasText(senderId);
        if (enabled && configured) {
            log.info("Bangladesh SMS provider configured with Anbernet API at {}", apiUrl);
        } else if (enabled) {
            log.warn("Bangladesh SMS provider is enabled but api-url/account/api-key/sender-id are incomplete");
        }
    }

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        if (!isEnabled()) {
            log.warn("Bangladesh SMS provider is not enabled");
            return false;
        }

        if (!configured) {
            log.error("Bangladesh SMS provider is not configured. Check api-url, account, api-key, and sender-id.");
            return false;
        }

        String normalizedToNumber = phoneNumberParser.normalizePhoneNumber(phoneNumber);

        Map<String, Object> body = new HashMap<>();
        body.put("account", account);
        body.put("api_key", apiKey);
        body.put("senderid", senderId);
        body.put("receivers", List.of(normalizedToNumber));
        body.put("msgdata", message);

        try {
            log.info("Sending SMS via Anbernet (Bangladesh) to: {} (normalized: {})", phoneNumber, normalizedToNumber);
            log.debug("SMS message: {}", message);

            String response = webClient.post()
                    .uri(apiUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            boolean success = isSuccessfulResponse(response);
            if (success) {
                log.info("SMS sent successfully via Anbernet (Bangladesh) to: {}. Response: {}", phoneNumber, response);
            } else {
                log.warn("Anbernet SMS response indicated failure for {}: {}", phoneNumber, response);
            }
            return success;
        } catch (WebClientResponseException e) {
            log.error("Anbernet API error sending SMS to {}: status={}, body={}",
                    phoneNumber, e.getStatusCode().value(), e.getResponseBodyAsString(), e);
            return false;
        } catch (Exception e) {
            log.error("Error sending SMS via Anbernet (Bangladesh) to {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
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

    @Override
    public String getProviderName() {
        return "BangladeshSmsProvider";
    }

    @Override
    public boolean supportsCountry(String countryCode) {
        if (countryCode == null) {
            return false;
        }

        if (supportedCountryCodes == null) {
            supportedCountryCodes = Arrays.stream(countryCodes.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .toList();
        }

        return supportedCountryCodes.contains(countryCode.trim());
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
