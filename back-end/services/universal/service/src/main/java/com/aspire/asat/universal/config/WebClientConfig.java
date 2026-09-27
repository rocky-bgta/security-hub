package com.aspire.asat.universal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    // Provide a WebClient.Builder bean so components that depend on the builder can autowire it
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
