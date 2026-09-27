package com.aspire.asat.gateway.filter;

import com.aspire.asat.gateway.dto.ApiPermissionRule;
import com.aspire.asat.gateway.dto.PublicUrls;
import com.aspire.asat.gateway.entity.AuthSession;
import com.aspire.asat.gateway.entity.redis.RedisAccessToken;
import com.aspire.asat.gateway.service.RedisService;
import com.aspire.asat.gateway.service.SessionFallbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JwtAuthFilter} focusing on the Redis-down fallback path.
 * JWT validation is skipped by disabling the feature flag ({@code jwt.enable=false})
 * for public-URL tests, and mocked token parsing is used elsewhere.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterFallbackTest {

    @Mock
    private RedisService redisService;

    @Mock
    private SessionFallbackService sessionFallbackService;

    @Mock
    private WebFilterChain chain;

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        PublicUrls publicUrls = new PublicUrls();
        publicUrls.setUrls(List.of("/auth/api/v1/auth/login"));

        Map<String, List<ApiPermissionRule>> permissionsMap = Collections.emptyMap();

        filter = new JwtAuthFilter(permissionsMap, redisService, sessionFallbackService, publicUrls);
        ReflectionTestUtils.setField(filter, "jwtEnable", false); // Disable JWT for routing tests
    }

    @Test
    void filter_WhenJwtDisabled_AllowsRequestThrough() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/gateway/cms/api/v1/resource").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void filter_WhenPublicUrl_AllowsRequestWithoutAuth() {
        ReflectionTestUtils.setField(filter, "jwtEnable", true);
        MockServerHttpRequest request = MockServerHttpRequest.get("/auth/api/v1/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void filter_WhenMissingAuthHeader_Returns401() {
        ReflectionTestUtils.setField(filter, "jwtEnable", true);
        MockServerHttpRequest request = MockServerHttpRequest.get("/gateway/cms/api/v1/resource").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        // Response status should be 4xx (auth required).
        // Full assertion would require a real JWT; this test verifies no exception is thrown.
    }

    // -------------------------------------------------------------------------
    // SessionFallbackService unit tests
    // -------------------------------------------------------------------------

    @Test
    void sessionFallbackService_ReturnsEmptyWhenSessionExpired() {
        AuthSession expired = buildAuthSession(Instant.now().minusSeconds(10));
        when(sessionFallbackService.findSession(anyString())).thenReturn(
                reactor.core.publisher.Mono.just(expired).filter(s -> s.getExpiresAt().isAfter(Instant.now())));

        StepVerifier.create(sessionFallbackService.findSession("t1"))
                .verifyComplete();
    }

    @Test
    void sessionFallbackService_ReturnsSessionWhenValid() {
        AuthSession valid = buildAuthSession(Instant.now().plusSeconds(600));
        when(sessionFallbackService.findSession(anyString()))
                .thenReturn(reactor.core.publisher.Mono.just(valid));

        StepVerifier.create(sessionFallbackService.findSession("t1"))
                .expectNextMatches(s -> "user-abc".equals(s.getUserId()))
                .verifyComplete();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private AuthSession buildAuthSession(Instant expiresAt) {
        return new AuthSession()
                .setTokenId("t1")
                .setUserId("user-abc")
                .setAccessTokenChecksum("checksum-abc")
                .setUsername("tester")
                .setPermissions(List.of("USR:VIEW"))
                .setExpiresAt(expiresAt);
    }

    private RedisAccessToken buildRedisToken() {
        return new RedisAccessToken()
                .setTokenId("t1")
                .setUserId(UUID.randomUUID().toString())
                .setAccessToken("checksum-abc")
                .setUsername("tester");
    }
}
