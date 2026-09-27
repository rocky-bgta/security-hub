package com.aspire.asat.gateway.ratelimiter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;

/**
 * Extends {@link RequestRateLimiterGatewayFilterFactory} to make rate-limiting
 * fail-open when Redis is unavailable.
 *
 * <p>Spring Cloud Gateway's built-in implementation propagates Redis errors as a
 * reactive failure, which causes HTTP 500 responses to clients even when the
 * underlying service is healthy. This wrapper adds {@code onErrorResume} so that
 * if the rate-limiter's Redis call throws (e.g. connection refused), the request
 * is allowed through rather than rejected.
 *
 * <p>Normal rate limiting is completely unchanged when Redis is available.
 */
@Slf4j
public class FailOpenRequestRateLimiterGatewayFilterFactory extends RequestRateLimiterGatewayFilterFactory {

    public FailOpenRequestRateLimiterGatewayFilterFactory(RateLimiter<?> defaultRateLimiter, KeyResolver resolver) {
        super(defaultRateLimiter, resolver);
    }

    @Override
    public GatewayFilter apply(Config config) {
        GatewayFilter original = super.apply(config);
        return (exchange, chain) ->
                original.filter(exchange, chain)
                        .onErrorResume(throwable -> {
                            log.warn("Rate limiter Redis error — allowing request through (fail-open): {}",
                                    throwable.getMessage());
                            return chain.filter(exchange);
                        });
    }
}
