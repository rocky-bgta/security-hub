package com.aspire.asat.billing.scheduler;

import com.aspire.asat.billing.service.PaymentReconciliationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for payment reconciliation job
 * Periodically checks Stripe API for payments that succeeded but weren't processed locally
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReconciliationScheduler {

    private final PaymentReconciliationService paymentReconciliationService;

    @Value("${payment.reconciliation.enabled:true}")
    private boolean reconciliationEnabled;

    /**
     * Scheduled task to reconcile Stripe payments
     * Runs every 30 minutes by default (configurable via properties)
     * Cron format: second minute hour day month weekday
     */
    @Scheduled(cron = "${payment.reconciliation.scheduler.cron:0 */30 * * * ?}")
    public void reconcilePayments() {
        if (!reconciliationEnabled) {
            log.debug("Payment reconciliation scheduler is disabled");
            return;
        }

        try {
            log.info("Starting scheduled payment reconciliation job");
            paymentReconciliationService.reconcileStripePayments();
            log.info("Completed scheduled payment reconciliation job");
        } catch (Exception e) {
            log.error("Error in payment reconciliation scheduler: {}", e.getMessage(), e);
        }
    }
}

