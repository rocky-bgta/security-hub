package com.aspire.asat.phishing.client;

import com.aspire.asat.phishing.dto.enums.BreachSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for InsecureWeb Dark Web Monitoring API.
 * Fetches breach data from external dark web monitoring service.
 */
@Component
@Slf4j
public class InsecureWebClient {

    @Value("${insecureweb.api.key:}")
    private String apiKey;

    @Value("${insecureweb.api.url:https://api.insecureweb.com/v1}")
    private String baseUrl;

    private final WebClient webClient;

    public InsecureWebClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    /**
     * Fetch breaches for a specific domain
     */
    public List<ExternalBreachDto> getBreachesForDomain(String domain) {
        log.info("Fetching breaches for domain: {}", domain);

        // TODO: Implement actual API call when API credentials are available
        // For now, return mock data for development

        /*
        return webClient.get()
                .uri(baseUrl + "/breaches/domain/{domain}", domain)
                .header("Authorization", "Bearer " + apiKey)
                .retrieve()
                .bodyToFlux(ExternalBreachDto.class)
                .collectList()
                .block();
        */

        log.warn("InsecureWeb API not configured. Returning empty list.");
        return new ArrayList<>();
    }

    /**
     * Fetch breaches for a specific email address
     */
    public List<ExternalRecipientBreachDto> getBreachesForEmail(String email) {
        log.info("Fetching breaches for email: {}", email);

        // TODO: Implement actual API call
        log.warn("InsecureWeb API not configured. Returning empty list.");
        return new ArrayList<>();
    }

    /**
     * Get detailed breach information
     */
    public ExternalBreachDto getBreachDetails(String breachId) {
        log.info("Fetching breach details for ID: {}", breachId);

        // TODO: Implement actual API call
        log.warn("InsecureWeb API not configured. Returning null.");
        return null;
    }

    /**
     * Sync breaches for all monitored domains
     */
    public SyncResult syncAllDomains(List<String> domains) {
        log.info("Syncing breaches for {} domains", domains.size());
        
        SyncResult result = SyncResult.builder()
                .domainsProcessed(domains.size())
                .newBreachesFound(0)
                .newRecipientsFound(0)
                .errorsEncountered(0)
                .startedAt(Instant.now())
                .build();

        for (String domain : domains) {
            try {
                List<ExternalBreachDto> breaches = getBreachesForDomain(domain);
                result.setNewBreachesFound(result.getNewBreachesFound() + breaches.size());
            } catch (Exception e) {
                log.error("Error syncing domain {}: {}", domain, e.getMessage());
                result.setErrorsEncountered(result.getErrorsEncountered() + 1);
            }
        }

        result.setCompletedAt(Instant.now());
        result.setSuccess(result.getErrorsEncountered() == 0);
        
        log.info("Sync completed: {} breaches found, {} errors", 
                result.getNewBreachesFound(), result.getErrorsEncountered());
        
        return result;
    }

    /**
     * Check if API is configured
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    // DTOs for external API responses

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExternalBreachDto {
        private String externalId;
        private String domain;
        private Instant dateOfBreach;
        private String breachName;
        private String description;
        private List<String> compromisedData;
        private int recipientCount;
        private String severity;

        public BreachSeverity getSeverityEnum() {
            if (severity == null) return BreachSeverity.LOW;
            switch (severity.toUpperCase()) {
                case "HIGH": return BreachSeverity.HIGH;
                case "MEDIUM": return BreachSeverity.MEDIUM;
                default: return BreachSeverity.LOW;
            }
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExternalRecipientBreachDto {
        private String breachName;
        private String email;
        private String firstName;
        private String lastName;
        private List<String> tags;
        private int breachCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncResult {
        private int domainsProcessed;
        private int newBreachesFound;
        private int newRecipientsFound;
        private int errorsEncountered;
        private Instant startedAt;
        private Instant completedAt;
        private boolean success;
    }
}
