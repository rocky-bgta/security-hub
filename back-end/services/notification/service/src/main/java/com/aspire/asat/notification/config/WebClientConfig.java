package com.aspire.asat.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient bean used by {@code RegistrationServiceClient} to call the registration
 * service's recipient-hierarchy API for role-based notification fan-out.
 */
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
