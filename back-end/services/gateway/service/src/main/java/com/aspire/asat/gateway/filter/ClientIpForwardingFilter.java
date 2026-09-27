package com.aspire.asat.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * Ensures X-Forwarded-For and X-Real-IP are forwarded to downstream services (e.g. CMS)
 * for GeoIP-based video language resolution. Resolves client IP from incoming request
 * and adds headers so backend services receive the real client IP.
 */
@Slf4j
@Component
@Order(Integer.MIN_VALUE + 1) // Run early, before routing
public class ClientIpForwardingFilter implements WebFilter {

    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_REAL_IP = "X-Real-IP";
    private static final String UNKNOWN = "unknown";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String clientIp = resolveClientIp(exchange);
        if (StringUtils.isBlank(clientIp)) {
            return chain.filter(exchange);
        }
        ServerWebExchange mutated = exchange.mutate()
                .request(r -> r.headers(h -> {
                    h.set(X_FORWARDED_FOR, clientIp);
                    h.set(X_REAL_IP, clientIp);
                }))
                .build();
        log.debug("Forwarding client IP {} to downstream: {}", clientIp, exchange.getRequest().getURI().getPath());
        return chain.filter(mutated);
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        String ip = firstIpFromHeader(headers.getFirst(X_FORWARDED_FOR));
        if (ip != null) return ip;
        ip = firstIpFromHeader(headers.getFirst(X_REAL_IP));
        if (ip != null) return ip;
        InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
        return (remote != null && remote.getAddress() != null)
                ? remote.getAddress().getHostAddress()
                : null;
    }

    private String firstIpFromHeader(String value) {
        if (StringUtils.isBlank(value) || UNKNOWN.equalsIgnoreCase(value)) {
            return null;
        }
        String first = value.split(",")[0].trim();
        return StringUtils.isNotBlank(first) ? first : null;
    }
}
