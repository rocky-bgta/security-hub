package com.aspire.asat.phishing.scheduler;

import com.aspire.asat.phishing.service.DashboardAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DashboardAggregationScheduler {

    private final DashboardAggregationService dashboardAggregationService;

    @Value("${dashboard.aggregation.enabled:true}")
    private boolean dashboardAggregationEnabled;

    @Scheduled(cron = "${dashboard.aggregation.cron:0 */30 * * * ?}")
    public void aggregateDashboardStats() {
        if (!dashboardAggregationEnabled) {
            return;
        }
        log.info("Starting dashboard aggregation scheduler run");
        dashboardAggregationService.aggregateForAllClients();
        log.info("Completed dashboard aggregation scheduler run");
    }
}
