package com.aspire.asat.billing;

import com.aspire.asat.common.config.aws.SsmParameterInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.aspire.asat")
@EnableScheduling
public class BillingApplication {
    public static void main(String[] args) {
        // SpringApplication.run(BillingApplication.class, args);
        SpringApplication app = new SpringApplication(BillingApplication.class);
        app.addInitializers(new SsmParameterInitializer());
        app.run(args);
    }
}