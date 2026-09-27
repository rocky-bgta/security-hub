package com.aspire.asat.cms;

import com.aspire.asat.common.config.aws.SsmParameterInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication(scanBasePackages = "com.aspire.asat")
@ConfigurationPropertiesScan   // if you use @ConfigurationProperties in common
@EnableScheduling
public class CmsApplication {
    public static void main(String[] args) {
        // SpringApplication.run(CmsApplication.class, args);
        SpringApplication app = new SpringApplication(CmsApplication.class);
        app.addInitializers(new SsmParameterInitializer());
        app.run(args);
    }
}
