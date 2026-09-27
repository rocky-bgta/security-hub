package com.aspire.asat.billing.config;

import com.aspire.asat.billing.constant.WebApiUrlConstants;
import com.aspire.asat.billing.exception.BillingServiceException;
import com.aspire.asat.common.constants.InternalServiceAuthConstants;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Component
public class InternalServiceAuthInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    private static final String CREATE_PATH = WebApiUrlConstants.INVOICE_API + WebApiUrlConstants.CREATE;
    private static final String UPDATE_PATH = WebApiUrlConstants.INVOICE_API + "/update/**";

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
        if (!authEnabled || !requiresInternalAuth(request)) {
            return true;
        }

        String providedKey = request.getHeader(InternalServiceAuthConstants.HEADER_NAME);
        if (!isValidKey(providedKey)) {
            log.warn("Rejected internal-only invoice request: method={}, path={}",
                    request.getMethod(), request.getServletPath());
            throw new BillingServiceException("Internal service authentication required", HttpStatus.FORBIDDEN);
        }
        return true;
    }

    private boolean requiresInternalAuth(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String method = request.getMethod();

        if (HttpMethod.POST.matches(method) && PATH_MATCHER.match(CREATE_PATH, servletPath)) {
            return true;
        }
        return HttpMethod.PUT.matches(method) && PATH_MATCHER.match(UPDATE_PATH, servletPath);
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
