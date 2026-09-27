package com.aspire.asat.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component  // Re-enabled to handle CORS at Gateway level
@Order(Integer.MIN_VALUE + 1) // Run right after FilterAuditLogger
public class SecurityInterceptor implements WebFilter {

    private static final String MAX_AGE = "3600";
    private static final String DEFAULT_CSP_HEADER =
            "default-src 'self'; "
            + "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://appsforoffice.microsoft.com https://code.jquery.com https://cdn.jsdelivr.net https://portal.securityawarenesstraining.ai http://portal.securityawarenesstraining.ai https://content.securityawarenesstraining.ai https://*.securityawarenesstraining.ai https://*.aspireelearning.com http://*.aspireelearning.com; "
            + "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdn.jsdelivr.net https://content.aspireelearning.com https://content.securityawarenesstraining.ai https://*.aspireelearning.com https://*.securityawarenesstraining.ai; "
            + "font-src 'self' data: https://fonts.gstatic.com https://content.aspireelearning.com https://content.securityawarenesstraining.ai https://*.aspireelearning.com https://*.securityawarenesstraining.ai; "
            + "img-src 'self' data: blob: https://content.aspireelearning.com https://content.securityawarenesstraining.ai https://*.aspireelearning.com https://*.securityawarenesstraining.ai; "
            + "worker-src 'self' blob: https://content.aspireelearning.com https://content.securityawarenesstraining.ai; "
            + "connect-src 'self' http: https: ws: wss:; "
            + "frame-src 'self' http: https:;";
    private static final String PHISHING_TRACKING_CSP_HEADER = "default-src 'self' data: blob: http: https:; script-src 'self' 'unsafe-inline' 'unsafe-eval' data: blob: http: https:; style-src 'self' 'unsafe-inline' data: blob: http: https:; font-src 'self' data: blob: http: https:; img-src 'self' data: blob: http: https:; connect-src 'self' data: blob: http: https: ws: wss:; frame-src 'self' data: blob: http: https:;";
    private static final String HTTPS = "https://";
    private static final String HTTP = "http://";

    private static final String CONTENT_SECURITY_POLICY = "Content-Security-Policy";
    private static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";

    @Value("${asat-service.allow-origins}")
    private String acAllowOrigins;

    @Value("${asat-service.allow-methods}")
    private String acAllowMethods;

    @Value("${asat-service.allow-headers}")
    private String acAllowHeaders;

    @Value("${asat-service.frontend-url}")
    private String frontEndUrl;


  //  @Override
//    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//        final var request = exchange.getRequest();
//        final var response = exchange.getResponse();
//        final var headers = response.getHeaders();
//
//        log.info("SecurityInterceptor running for path: {}, method: {}, full URI: {}", request.getPath(), request.getMethod(), request.getURI());
//
//        // Log existing CORS headers before processing
//        log.info("Existing CORS headers before processing: {}", headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
//
//        // Get the request origin - try multiple ways to extract it
//        String requestOrigin = request.getHeaders().getFirst(HttpHeaders.ORIGIN);
//        if (requestOrigin == null) {
//            requestOrigin = request.getHeaders().getFirst("origin");
//        }
//        if (requestOrigin == null) {
//            requestOrigin = request.getHeaders().getFirst("Origin");
//        }
//        log.info("Request origin: {}, configured allow-origins: {}", requestOrigin, acAllowOrigins);
//        log.info("All request headers: {}", request.getHeaders());
//
//        // Add CORS headers - use request origin if present, otherwise use configured origin
//        String corsOrigin = getCorsOrigin(requestOrigin);
//
//        // Always clear existing CORS headers first to prevent duplicates
//        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
//        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
//        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS);
//        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
//        headers.remove(HttpHeaders.ACCESS_CONTROL_MAX_AGE);
//        headers.remove(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS);
//
//        // Now set our CORS headers
//        // For CORS with credentials=true, we must use the specific origin, not '*'
//        String actualCorsOrigin;
//        if ("*".equals(corsOrigin)) {
//            // Use the actual request origin for credentials=true compatibility
//            actualCorsOrigin = requestOrigin != null ? requestOrigin : "*";
//            log.info("Using wildcard origin detected, setting actual origin to: {}", actualCorsOrigin);
//        } else {
//            actualCorsOrigin = corsOrigin;
//            log.info("Using configured origin: {}", actualCorsOrigin);
//        }
//
//        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, actualCorsOrigin);
//
//        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, acAllowMethods);
//        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, acAllowHeaders);
//        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
//        headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
//
//        // Add common headers that might be needed by the frontend
//        //headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization, Content-Type, X-Requested-With, Accept, Origin, Access-Control-Allow-Origin");
//
//        log.info("Set CORS headers - Origin: {}, Methods: {}, Headers: {}", corsOrigin, acAllowMethods, acAllowHeaders);
//
//        // Add security headers
//        headers.set(CONTENT_SECURITY_POLICY, HEADER);
//        headers.set(STRICT_TRANSPORT_SECURITY, "max-age=31536000; includeSubDomains");
//        headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
//                "Authorization, Content-Type, X-Requested-With, Accept, Origin, Access-Control-Allow-Origin, Set-Cookie");
//
//
//        // Log final CORS headers after processing
//        log.info("Final CORS headers after processing: {}", headers.get(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
//
//        if (request.getMethod() == HttpMethod.OPTIONS) {
//            log.info("Handling OPTIONS request in SecurityInterceptor");
//            response.setStatusCode(HttpStatus.OK);
//            return response.setComplete();
//        }
//
//
//        log.info("Continuing with request chain");
//        return chain.filter(exchange);
//    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        final var request = exchange.getRequest();
        final var response = exchange.getResponse();
        final var headers = response.getHeaders();

        log.info("SecurityInterceptor running for path: {}, method: {}, URI: {}", request.getPath(), request.getMethod(), request.getURI());

        String requestOrigin = request.getHeaders().getFirst(HttpHeaders.ORIGIN);
        log.info("Request origin: {}, Allowed origins: {}", requestOrigin, acAllowOrigins);

        String corsOrigin = getCorsOrigin(requestOrigin);

        // clear
        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS);
        headers.remove(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS);
        headers.remove(HttpHeaders.ACCESS_CONTROL_MAX_AGE);
        headers.remove(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS);

        // safety net for wildcard headers
        String effectiveAllowHeaders = acAllowHeaders;
        if ("*".equals(effectiveAllowHeaders)) {
            effectiveAllowHeaders = "Authorization, Content-Type, Accept, Origin, X-Requested-With, Access-Control-Allow-Origin";
        }

        // set
        if (corsOrigin != null) {
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, corsOrigin);
            headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        }
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, acAllowMethods);
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, effectiveAllowHeaders);
        headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
        headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                "Authorization, Content-Type, X-Requested-With, Accept, Origin, Access-Control-Allow-Origin, Set-Cookie");

        // Security headers (relaxed for phishing tracking page to allow imported assets)
        headers.set(CONTENT_SECURITY_POLICY, resolveCspHeader(request.getPath().value()));
        headers.set(STRICT_TRANSPORT_SECURITY, "max-age=31536000; includeSubDomains");

        if (request.getMethod() == HttpMethod.OPTIONS) {
            if (requestOrigin != null && corsOrigin == null) {
                response.setStatusCode(HttpStatus.FORBIDDEN);
                return response.setComplete();
            }
            log.info("Handling OPTIONS request");
            response.setStatusCode(HttpStatus.OK);
            return response.setComplete();
        }

        return chain.filter(exchange);
    }



    private String getCorsOrigin(String requestOrigin) {
        if (requestOrigin == null || requestOrigin.isEmpty()) {
            return null;
        }

        // Always log the protocol for debugging
        String protocol = requestOrigin.startsWith(HTTPS) ? "HTTPS" : 
                        requestOrigin.startsWith(HTTP) ? "HTTP" : "UNKNOWN";
        log.info("Processing {} request from origin: {}", protocol, requestOrigin);

        if ("*".equals(acAllowOrigins)) {
            log.info("Development mode - allowing all origins including {}: {}", protocol, requestOrigin);
            return requestOrigin;
        }

        String[] allowedOrigins = acAllowOrigins.split(",");
        String trimmedRequest = requestOrigin.trim();

        for (String allowedOrigin : allowedOrigins) {
            String trimmedAllowed = allowedOrigin.trim();

            if (trimmedRequest.equals(trimmedAllowed)) {
                log.info("Request origin {} is in allowed list", requestOrigin);
                return requestOrigin;
            }

            if (trimmedAllowed.contains("*") && matchesWildcardPattern(trimmedRequest, trimmedAllowed)) {
                log.info("Wildcard pattern match for origin: {} allowing {}", allowedOrigin, requestOrigin);
                return requestOrigin;
            }

            if (isProtocolVariantMatch(trimmedAllowed, trimmedRequest)) {
                log.info("Protocol variant match for origin: {} allowing {}", allowedOrigin, requestOrigin);
                return requestOrigin;
            }
        }

        log.warn("Request origin {} not in allowed list", requestOrigin);
        return null;
    }

    private boolean isProtocolVariantMatch(String allowed, String request) {
        // Check if they're the same domain but different protocols
        String allowedDomain = allowed.replaceFirst("^https?://", "");
        String requestDomain = request.replaceFirst("^https?://", "");
        
        boolean domainsMatch = allowedDomain.equals(requestDomain);
        boolean protocolsDiffer = (allowed.startsWith(HTTPS) && request.startsWith(HTTP)) ||
                                 (allowed.startsWith(HTTP) && request.startsWith(HTTPS));
        
        if (domainsMatch && protocolsDiffer) {
            log.info("Protocol variant match found: {} ({}) and {} ({})", 
                    allowed, allowed.startsWith(HTTPS) ? "HTTPS" : "HTTP",
                    request, request.startsWith(HTTPS) ? "HTTPS" : "HTTP");
            return true;
        }
        
        return false;
    }

    private boolean matchesWildcardPattern(String requestOrigin, String wildcardPattern) {
        try {
            // Convert wildcard pattern to regex
            String regexPattern = wildcardPattern
                    .replace(".", "\\.")
                    .replace("*", ".*");

            return requestOrigin.matches(regexPattern);
        } catch (Exception e) {
            log.warn("Error matching wildcard pattern: {} against origin: {}", wildcardPattern, requestOrigin);
            return false;
        }
    }

    private String resolveCspHeader(String requestPath) {
        if (requestPath == null || requestPath.isBlank()) {
            return DEFAULT_CSP_HEADER;
        }

        // Imported phishing pages often reference third-party CSS/JS assets.
        // Keep default restrictive policy globally and relax only for tracking pages.
        if (requestPath.contains("/t/phish/") || requestPath.contains("/t/submit/")) {
            return PHISHING_TRACKING_CSP_HEADER;
        }

        // Outlook task pane: office.js loads additional scripts (CDN chunks, workers). Default CSP can block them.
        if (requestPath.contains("/outlook-addin/")) {
            return PHISHING_TRACKING_CSP_HEADER;
        }

        return DEFAULT_CSP_HEADER;
    }
}
