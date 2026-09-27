package com.aspire.asat.phishing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /**
     * Upper bound (in MB) on the response body the reactive codecs will buffer in
     * memory. The Spring default is 256 KB, which is too small for downloading
     * rendered deepfake videos (HeyGen MP4s are several MB), so it is raised here.
     */
    @Value("${phishing.webclient.max-in-memory-size-mb:64}")
    private int maxInMemorySizeMb;

    @Bean
    public WebClient webClient() {
        int maxBytes = maxInMemorySizeMb * 1024 * 1024;
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxBytes))
                .build();
        return WebClient.builder()
                .exchangeStrategies(strategies)
                .build();
    }
}
