package com.aspire.asat.vps;

import com.aspire.asat.common.config.aws.SsmParameterInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@SpringBootApplication()
public class VpsApplication {
    public static void main(String[] args) {
        // SpringApplication.run(VpsApplication.class, args);
        SpringApplication app = new SpringApplication(VpsApplication.class);
        app.addInitializers(new SsmParameterInitializer());
        app.run(args);
    }
}
