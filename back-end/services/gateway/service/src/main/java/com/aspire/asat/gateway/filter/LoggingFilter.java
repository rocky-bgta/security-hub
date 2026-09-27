package com.aspire.asat.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@Order(Integer.MIN_VALUE + 2) // Run after SecurityInterceptor
public class LoggingFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Instant startTime = Instant.now();

        String requestPath = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        String queryString = exchange.getRequest().getURI().getQuery();
        String fullUrl = queryString != null ? requestPath + "?" + queryString : requestPath;

        log.info("➡ Request started: {} {} at {}", method, requestPath, startTime);
        log.debug("Full request details - Method: {}, Path: {}, Query: {}, Headers: {}", 
                method, requestPath, queryString, exchange.getRequest().getHeaders().toSingleValueMap());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            Instant endTime = Instant.now();
            long durationMillis = Duration.between(startTime, endTime).toMillis();

            int statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value()
                    : 0;

            log.info("✅ Response completed: {} {} | Status: {} | Duration: {} ms", method, requestPath, statusCode, durationMillis);
        }));
    }
}
