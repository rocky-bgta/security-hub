package com.aspire.asat.billing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "billing.invoice.expiry")
public class InvoiceExpiryProperties {
    private boolean enabled = true;
    private int days = 30;
    private String cron = "0 0 * * * ?";
}
