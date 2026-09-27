package com.aspire.asat.breachdetection;

import com.aspire.asat.common.config.aws.SsmParameterInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableAsync
@SpringBootApplication(scanBasePackages = "com.aspire.asat")
@ConfigurationPropertiesScan
public class BreachDetectionApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(BreachDetectionApplication.class);
        app.addInitializers(new SsmParameterInitializer());
        app.run(args);
    }
}
