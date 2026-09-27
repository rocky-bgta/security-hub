package com.aspire.asat.universal.client.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for WebClient used in CommonHttpClient
 */
@Slf4j
@Configuration
public class HttpClientConfig {

    private static final int CONNECTION_TIMEOUT = 10000; // 10 seconds
    private static final int READ_TIMEOUT = 30; // 30 seconds
    private static final int WRITE_TIMEOUT = 30; // 30 seconds
    private static final int MAX_IN_MEMORY_SIZE = 10 * 1024 * 1024; // 10MB

    @Bean
    public WebClient webClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, CONNECTION_TIMEOUT)
                .responseTimeout(Duration.ofSeconds(READ_TIMEOUT))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(READ_TIMEOUT, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(WRITE_TIMEOUT, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_IN_MEMORY_SIZE))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    /**
     * Filter to log request details
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                values.forEach(value -> log.debug("Request Header: {}={}", name, value))
            );
            return Mono.just(clientRequest);
        });
    }

    /**
     * Filter to log response details
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Response Status Code: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}

