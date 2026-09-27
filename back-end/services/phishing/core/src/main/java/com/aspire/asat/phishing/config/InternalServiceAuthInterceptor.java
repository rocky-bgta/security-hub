package com.aspire.asat.phishing.config;

import com.aspire.asat.common.constants.InternalServiceAuthConstants;
import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import com.aspire.asat.phishing.exception.ServiceException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Authenticates service-to-service calls to {@code /api/v1/phishing/internal/**} only.
 * Tracking and public phishing endpoints are intentionally not covered.
 */
@Slf4j
@Component
public class InternalServiceAuthInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String INTERNAL_PATH_PATTERN = WebApiUrlConstants.INTERNAL_API_PATH + "/**";

    @Value("${internal.service.api-key:}")
    private String apiKey;

    @Value("${internal.service.auth-enabled:true}")
    private boolean authEnabled;

    @PostConstruct
    void validateConfiguration() {
        if (authEnabled && (apiKey == null || apiKey.isBlank())) {
            throw new IllegalStateException(
                    "internal.service.api-key must be configured when internal.service.auth-enabled is true");
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!authEnabled) {
            return true;
        }
        String servletPath = request.getServletPath();
        if (!PATH_MATCHER.match(INTERNAL_PATH_PATTERN, servletPath)) {
            return true;
        }

        String providedKey = request.getHeader(InternalServiceAuthConstants.HEADER_NAME);
        if (!isValidKey(providedKey)) {
            log.warn("Rejected internal phishing request: method={}, path={}",
                    request.getMethod(), servletPath);
            throw new ServiceException("Internal service authentication required", HttpStatus.FORBIDDEN);
        }
        return true;
    }

    private boolean isValidKey(String providedKey) {
        if (providedKey == null || providedKey.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(
                apiKey.getBytes(StandardCharsets.UTF_8),
                providedKey.getBytes(StandardCharsets.UTF_8));
    }
}
