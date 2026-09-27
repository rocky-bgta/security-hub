package com.aspire.asat.phishing;

import com.aspire.asat.common.config.aws.SsmParameterInitializer;
import io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@EnableAsync
@SpringBootApplication(scanBasePackages = "com.aspire.asat")
@ConfigurationPropertiesScan
public class PhishingApplication {
    public static void main(String[] args) {
        // SpringApplication.run(PhishingApplication.class, args);
        SpringApplication app = new SpringApplication(PhishingApplication.class);
        app.addInitializers(new SsmParameterInitializer());
        app.run(args);
    }
}

