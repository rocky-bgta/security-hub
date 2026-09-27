package com.aspire.asat.phishing.scheduler;

import com.aspire.asat.phishing.client.InsecureWebClient;
import com.aspire.asat.phishing.model.BreachDetectionConfig;
import com.aspire.asat.phishing.repository.BreachDetectionConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Scheduler for automatic breach data synchronization.
 * Runs periodically to fetch new breach data from InsecureWeb API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BreachSyncScheduler {

    private final BreachDetectionConfigRepository configRepository;
    private final InsecureWebClient insecureWebClient;

    /**
     * Sync breach data every 6 hours.
     * This runs for all clients with breach collection enabled.
     */
    @Scheduled(cron = "0 0 */6 * * *")  // Every 6 hours
    public void syncBreachData() {
        log.info("Starting scheduled breach data sync");

        try {
            // Find all configs with collection enabled
            List<BreachDetectionConfig> configs = configRepository.findAllWithCollectionEnabled();

            if (configs.isEmpty()) {
                log.info("No clients have breach collection enabled");
                return;
            }

            log.info("Found {} clients with breach collection enabled", configs.size());

            for (BreachDetectionConfig config : configs) {
                try {
                    syncForClient(config);
                } catch (Exception e) {
                    log.error("Error syncing breaches for client {}: {}",
                            config.getClientId(), e.getMessage());
                }
            }

            log.info("Completed scheduled breach data sync");

        } catch (Exception e) {
            log.error("Error in breach sync scheduler", e);
        }
    }

    /**
     * Sync breach data for a specific client configuration.
     */
    private void syncForClient(BreachDetectionConfig config) {
        String clientId = config.getClientId();
        List<String> domains = config.getMonitoredDomains();

        if (domains == null || domains.isEmpty()) {
            log.debug("No domains configured for client {}", clientId);
            return;
        }

        log.info("Syncing {} domains for client {}", domains.size(), clientId);

        // Check if API is configured
        if (!insecureWebClient.isConfigured()) {
            log.warn("InsecureWeb API not configured, skipping sync");
            return;
        }

        // Sync all domains
        InsecureWebClient.SyncResult result = insecureWebClient.syncAllDomains(domains);

        // Update last sync time
        config.setLastSyncAt(Instant.now());
        configRepository.save(config);

        log.info("Sync completed for client {}: {} breaches found, {} errors",
                clientId, result.getNewBreachesFound(), result.getErrorsEncountered());
    }

    /**
     * Daily cleanup of old resolved breaches (optional).
     * Keeps data manageable by archiving old resolved records.
     */
    @Scheduled(cron = "0 0 2 * * *")  // Every day at 2 AM
    public void cleanupOldBreaches() {
        log.info("Starting breach data cleanup");

        // TODO: Implement archival of breaches resolved more than 1 year ago
        // This is optional and depends on data retention requirements

        log.info("Breach data cleanup completed");
    }
}
