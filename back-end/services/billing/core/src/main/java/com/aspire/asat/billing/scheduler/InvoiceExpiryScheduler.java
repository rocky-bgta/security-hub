package com.aspire.asat.billing.scheduler;

import com.aspire.asat.billing.service.InvoiceExpiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceExpiryScheduler {

    private final InvoiceExpiryService invoiceExpiryService;

    @Value("${billing.invoice.expiry.enabled:true}")
    private boolean expiryEnabled;

    @Scheduled(cron = "${billing.invoice.expiry.cron:0 0 * * * ?}")
    public void expireOverdueInvoices() {
        if (!expiryEnabled) {
            log.debug("Invoice expiry scheduler is disabled");
            return;
        }

        try {
            log.info("Starting unpaid invoice expiry job");
            int expired = invoiceExpiryService.expireOverdueInvoices();
            log.info("Completed unpaid invoice expiry job. Expired: {}", expired);
        } catch (Exception e) {
            log.error("Error in unpaid invoice expiry scheduler: {}", e.getMessage(), e);
        }
    }
}
