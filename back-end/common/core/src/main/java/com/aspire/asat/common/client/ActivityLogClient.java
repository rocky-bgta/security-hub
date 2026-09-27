package com.aspire.asat.common.client;

import com.aspire.asat.common.dto.activitylog.CreateActivityLogDto;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ActivityLogClient {
    private final WebClient webClient;

    @Value("${service.registration.url:${client.registration.url:}}")
    private String registrationServiceUrl;

    @Async
    public void createActivityLog(CreateActivityLogDto requestDto) {
        try {
            String url = registrationServiceUrl + "/activity-log";
            log.debug("Calling registration service to create activity log: {}", url);
            
            webClient.post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(Mono.just(requestDto), CreateActivityLogDto.class)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnSuccess(response -> log.debug("Successfully created activity log with status: {}", response.getStatusCode()))
                    .doOnError(error -> log.error("Failed to create activity log", error))
                    .subscribe();
        } catch (Exception e) {
            log.error("Error calling registration service to create activity log: {}", e.getMessage(), e);
            // Don't throw exception to avoid disrupting the main flow
        }
    }
}

