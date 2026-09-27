package com.aspire.asat.registration;

import com.aspire.asat.common.config.aws.SsmParameterInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "com.aspire.asat")
@ConfigurationPropertiesScan   // if you use @ConfigurationProperties in common
public class RegistrationApplication {
    public static void main(String[] args) {
        // SpringApplication.run(RegistrationApplication.class, args);
        SpringApplication app = new SpringApplication(RegistrationApplication.class);
        app.addInitializers(new SsmParameterInitializer());
        app.run(args);
    }
}
