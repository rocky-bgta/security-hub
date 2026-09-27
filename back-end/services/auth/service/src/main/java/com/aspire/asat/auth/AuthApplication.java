package com.aspire.asat.auth;

import com.aspire.asat.common.config.aws.SsmParameterInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;


@SpringBootApplication(scanBasePackages = "com.aspire.asat", exclude = {UserDetailsServiceAutoConfiguration.class})
@ConfigurationPropertiesScan
public class AuthApplication {
    public static void main(String[] args) {
        // SpringApplication.run(AuthApplication.class, args);
        SpringApplication app = new SpringApplication(AuthApplication.class);
        app.addInitializers(new SsmParameterInitializer());
        app.run(args);
    }
}
