package com.aspire.asat.notification.client;

import com.aspire.asat.common.dto.notification.NotificationRecipientBundleDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

/**
 * Client for the registration service's recipient-hierarchy API, used to resolve the full
 * fan-out target set (user, client admin, MSP, Aspire Admins) for a role-based notification
 * event.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RegistrationServiceClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${service.registration.url}")
    private String registrationUrl;

    /**
     * Fetch the notification recipient bundle for a user from the registration service.
     * Returns empty if the user cannot be resolved or the registration service is unreachable
     * -- callers should fail closed for the whole event in that case (log and skip).
     */
    public Optional<NotificationRecipientBundleDto> getNotificationRecipientBundle(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("getNotificationRecipientBundle called with blank userId");
            return Optional.empty();
        }

        String url = registrationUrl + "/end-user/notification-data?userId=" + userId.trim();
        log.debug("Fetching notification recipient bundle from registration: {}", url);

        try {
            JsonNode response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response == null || response.get("data") == null || response.get("data").isNull()) {
                log.warn("No notification recipient data returned from registration for userId: {}", userId);
                return Optional.empty();
            }

            NotificationRecipientBundleDto bundle = objectMapper.treeToValue(response.get("data"), NotificationRecipientBundleDto.class);
            return Optional.ofNullable(bundle);
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Registration service reports user not found for notification recipient bundle: {}", userId);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching notification recipient bundle for userId {}: {}", userId, e.getMessage(), e);
            return Optional.empty();
        }
    }
}
