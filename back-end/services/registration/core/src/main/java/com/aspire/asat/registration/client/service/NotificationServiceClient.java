package com.aspire.asat.registration.client.service;

import com.aspire.asat.registration.data.notification.NotificationRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class NotificationServiceClient {
    private final WebClient webClient;

    @Value("${client.notification.url}")
    private String notificationServiceUrl;

    public NotificationServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Async
    public void sendEmail(NotificationRequestDto requestDto) {
        log.info("Sending email request to notification service for recipient: {}", requestDto.getTo());
        webClient.post()
                .uri(notificationServiceUrl + "/api/v1/send") // Assuming the path from your curl
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(requestDto), NotificationRequestDto.class)
                .retrieve()
                .toBodilessEntity() // We only care if the request was accepted (2xx), not the body
                .doOnSuccess(response -> log.info("Successfully sent request to notification service with status: {}", response.getStatusCode()))
                .doOnError(error -> log.error("Failed to send request to notification service", error))
                .subscribe(); // Use subscribe() for fire-and-forget non-blocking calls
    }
}
