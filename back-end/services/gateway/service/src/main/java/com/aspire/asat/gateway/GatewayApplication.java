package com.aspire.asat.gateway;

import com.aspire.asat.common.config.aws.SsmParameterInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@Slf4j
@EnableCaching
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        log.info("Gateway Application Started ......................... ");
        // SpringApplication.run(GatewayApplication.class, args);
        SpringApplication app = new SpringApplication(GatewayApplication.class);
        app.addInitializers(new SsmParameterInitializer());
        app.run(args);
    }
}
