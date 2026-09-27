package com.aspire.asat.gateway.config;

import com.aspire.asat.gateway.ratelimiter.FailOpenRequestRateLimiterGatewayFilterFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Slf4j
@Configuration
public class RateLimiterConfig {

    private static final String DEFAULT_IP = "unknown";

    /**
     * Replaces the default {@link RequestRateLimiterGatewayFilterFactory} with a
     * fail-open variant that allows requests through instead of returning HTTP 500
     * when Redis is unavailable.
     */
    @Bean
    public RequestRateLimiterGatewayFilterFactory requestRateLimiterGatewayFilterFactory(
            RateLimiter<?> defaultRateLimiter,
            KeyResolver resolver) {
        return new FailOpenRequestRateLimiterGatewayFilterFactory(defaultRateLimiter, resolver);
    }

    @Bean(name = "ipKeyResolver")
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String clientIp = null;
            
            // First, try to get IP from X-Forwarded-For header (set by nginx/proxy)
            String xForwardedFor = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-Forwarded-For");
            if (!ObjectUtils.isEmpty(xForwardedFor)) {
                // X-Forwarded-For can contain multiple IPs, take the first one (original client)
                clientIp = xForwardedFor.split(",")[0].trim();
            }
            
            // Then try X-Real-IP header (also set by nginx)
            if (ObjectUtils.isEmpty(clientIp)) {
                clientIp = exchange.getRequest()
                        .getHeaders()
                        .getFirst("X-Real-IP");
            }
            
            // Fall back to remote address if headers are not present
            if (ObjectUtils.isEmpty(clientIp)) {
                InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
                if (remoteAddress != null && remoteAddress.getAddress() != null) {
                    clientIp = remoteAddress.getAddress().getHostAddress();
                }
            }
            
            // Use default if still null
            if (ObjectUtils.isEmpty(clientIp)) {
                clientIp = DEFAULT_IP;
            }
            
            String path = exchange.getRequest().getURI().getPath();
            String method = exchange.getRequest().getMethod().name();
            log.debug("Rate limiting request: {} {} from IP: {}", method, path, clientIp);
            
            return reactor.core.publisher.Mono.just(clientIp);
        };
    }
}

