package com.aspire.asat.breachdetection.scheduler;

import com.aspire.asat.breachdetection.model.BreachDetectionConfig;
import com.aspire.asat.breachdetection.repository.BreachDetectionConfigRepository;
import com.aspire.asat.breachdetection.service.BreachDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BreachSyncScheduler {

    private final BreachDetectionConfigRepository configRepository;
    private final BreachDetectionService breachDetectionService;

    @Scheduled(cron = "0 0 */6 * * *")
    public void syncBreachData() {
        try {
            List<BreachDetectionConfig> configs = configRepository.findAllWithCollectionEnabled();
            for (BreachDetectionConfig config : configs) {
                try {
                    breachDetectionService.triggerScheduledSync(config);
                } catch (Exception ex) {
                    log.error("Error syncing breaches for client {}: {}", config.getClientId(), ex.getMessage(), ex);
                }
            }
        } catch (Exception ex) {
            log.error("Error in breach sync scheduler", ex);
        }
    }
}
