package com.aspire.asat.gateway.filter;

import com.aspire.asat.gateway.dto.ApiPermissionRule;
import com.aspire.asat.gateway.dto.PublicUrls;
import com.aspire.asat.gateway.dto.SessionResolution;
import com.aspire.asat.gateway.dto.enums.ErrorMessages;
import com.aspire.asat.gateway.dto.enums.SpecialChars;
import com.aspire.asat.gateway.entity.redis.RedisAccessToken;
import com.aspire.asat.gateway.model.CurrentUserContext;
import com.aspire.asat.gateway.service.RedisService;
import com.aspire.asat.gateway.service.SessionFallbackService;
import com.aspire.asat.gateway.util.ChecksumUtil;
import com.aspire.asat.gateway.util.CommonUtil;
import com.aspire.asat.gateway.util.CustomDataConfiguration;
import com.aspire.asat.gateway.util.JWTUtils;
import com.aspire.asat.gateway.util.JacksonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * JWT authentication and authorization filter.
 *
 * <p><b>Redis-healthy path (normal operation):</b><br>
 * The filter reads session data from Redis (blocking, on a bounded-elastic thread),
 * performs the checksum validation, single-active-session enforcement, and RBAC check,
 * then forwards an enriched {@code CurrentContext} header to the downstream service.
 *
 * <p><b>Redis-down path (fallback):</b><br>
 * When Redis returns {@code null} or throws, the filter falls back to the MongoDB
 * {@code auth_sessions} collection via {@link SessionFallbackService}. The checksum
 * and RBAC checks are identical on the fallback path. Single-active-session enforcement
 * is deliberately skipped when serving from MongoDB (it requires a Redis-only mapping)
 * and a warning is logged.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("unused")
@Order(Integer.MIN_VALUE + 3)
public class JwtAuthFilter implements WebFilter {

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final Map<String, List<ApiPermissionRule>> apiPermissionsRuleMap;
    private final RedisService redisService;
    private final SessionFallbackService sessionFallbackService;
    private final PublicUrls publicUrls;

    public static final String ALL_PERMISSION = "ALL";
    public static final String BLOCKED_PERMISSION = "BLOCKED";

    @Value("${jwt.secret}")
    private String jwtSecretKey;

    @Value("${jwt.enable:true}")
    private boolean jwtEnable;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.info("Client IP: {}", CommonUtil.getClientRealIpAddress(request));

        if (!jwtEnable) {
            return chain.filter(exchange);
        }

        if (request.getMethod() == HttpMethod.OPTIONS) {
            log.info("Allowing OPTIONS request for CORS preflight: {}", path);
            return chain.filter(exchange);
        }

        log.info("Actual URL requested: {}", path);

        if (isPublicUrl(path)) {
            return chain.filter(exchange);
        }

        String authorizationHeader = FilterValidationAndMapper.bearerAccessToken(request);
        if (authorizationHeader == null) {
            return FilterValidationAndMapper.onError(exchange, ErrorMessages.AUTH_HEADER_MISSING);
        }

        String jwtToken = extractJwtToken(authorizationHeader);
        if (jwtToken == null) {
            return FilterValidationAndMapper.onError(exchange, ErrorMessages.AUTH_HEADER_MISSING);
        }

        String tokenId;
        try {
            tokenId = JWTUtils.extractTokenId(jwtToken, jwtSecretKey);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return FilterValidationAndMapper.onError(exchange, ErrorMessages.AUTH_TOKEN_EXPIRED);
        }

        return resolveSession(tokenId)
                .flatMap(resolution -> processAuthorization(exchange, chain, jwtToken, tokenId, path, authorizationHeader, resolution));
    }

    // -------------------------------------------------------------------------
    // Session resolution — Redis-first, MongoDB-fallback
    // -------------------------------------------------------------------------

    /**
     * Resolves session + permissions from Redis (blocking, on boundedElastic scheduler)
     * and falls back to MongoDB when Redis is unavailable or returns no data.
     */
    private Mono<SessionResolution> resolveSession(String tokenId) {
        return Mono.fromCallable(() -> {
            try {
                RedisAccessToken token = redisService.accessTokenSafe(tokenId);
                if (token == null) {
                    // Key not found or Redis down — signal fallback.
                    return Optional.<SessionResolution>empty();
                }
                String userId = token.getUserId();
                String activeTokenId = redisService.getActiveTokenIdForUserSafe(userId);
                List<String> permissions = redisService.getPermissionsSafe(tokenId);
                return Optional.of(new SessionResolution(token, permissions, activeTokenId, false));
            } catch (Exception e) {
                log.warn("Unexpected Redis error during session resolution, will attempt MongoDB fallback: {}", e.getMessage());
                return Optional.<SessionResolution>empty();
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(opt -> {
            return opt.<Mono<? extends SessionResolution>>map(Mono::just).orElseGet(() -> sessionFallbackService.findSession(tokenId)
                    .map(authSession -> new SessionResolution(
                            authSession.toRedisAccessToken(),
                            authSession.getPermissions() != null ? authSession.getPermissions() : List.of(),
                            null,   // activeTokenId unknown — single-session check will be skipped
                            true))
                    .defaultIfEmpty(new SessionResolution(null, List.of(), null, true)));
            // Redis unavailable or token not found — fall back to MongoDB.
        });
    }

    // -------------------------------------------------------------------------
    // Authorization processing
    // -------------------------------------------------------------------------

    private Mono<Void> processAuthorization(
            ServerWebExchange exchange,
            WebFilterChain chain,
            String jwtToken,
            String tokenId,
            String path,
            String authorizationHeader,
            SessionResolution resolution) {

        RedisAccessToken redisAccessToken = resolution.accessToken();

        if (redisAccessToken == null || !ChecksumUtil.verifyChecksum(jwtToken, redisAccessToken.getAccessToken())) {
            return FilterValidationAndMapper.onError(exchange, ErrorMessages.SESSION_TIMEOUT_IN_REDIS);
        }

        String userId = redisAccessToken.getUserId();

        if (!resolution.fromFallback()) {
            // Normal Redis path — enforce single active session.
            String activeTokenId = resolution.activeTokenId();
            if (activeTokenId == null || !activeTokenId.equals(tokenId)) {
                log.warn("Token {} is not the active token for userId: {}. Active token: {}", tokenId, userId, activeTokenId);
                return FilterValidationAndMapper.onError(exchange, ErrorMessages.SESSION_TIMEOUT_IN_REDIS);
            }
        } else {
            // MongoDB fallback path — single-session enforcement is not possible without Redis.
            log.warn("Redis unavailable — single-session enforcement skipped for userId: {}", userId);
        }

        List<String> scopes = resolution.permissions();

        if (!isAuthorized(path, scopes)) {
            log.warn("Unauthorized access attempt by tokenId: {} to path: {}", tokenId, path);
            return FilterValidationAndMapper.onError(exchange, ErrorMessages.UNAUTHORIZED_ACCESS);
        }

        CurrentUserContext currentUserContext = new CurrentUserContext();
        BeanUtils.copyProperties(redisAccessToken, currentUserContext);
        if (currentUserContext.getUsername() == null) {
            return FilterValidationAndMapper.onError(exchange, ErrorMessages.INVALID_AUTH_TOKEN);
        }

        currentUserContext.setRoles(redisAccessToken.getRoles());

        String base64UserCurrentContext = encodeCurrentUserContext(currentUserContext);
        if (base64UserCurrentContext == null) {
            return FilterValidationAndMapper.onError(exchange, ErrorMessages.AUTH_HEADER_MISS_MATCH);
        }

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.headers(h -> {
                    h.set(CustomDataConfiguration.HEADER_AUTHORIZATION, authorizationHeader);
                    h.set(CustomDataConfiguration.HEADER_CURRENT_USER_CONTEXT, base64UserCurrentContext);
                    h.set(CustomDataConfiguration.HEADER_CO_RELATION_ID, currentUserContext.getCoRelationId());
                }))
                .build();

        return chain.filter(mutatedExchange);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean isAuthorized(String path, List<String> userScopes) {
        String normalizedPath = path.startsWith("/gateway/") ? path.substring(8) : path;

        log.info("Actual Path requested: {}", path);
        log.info("Normalized path for authorization: {}", normalizedPath);

        String[] pathParts = normalizedPath.split("/");
        if (pathParts.length < 2) {
            log.error("Invalid path: {}", path);
            return false;
        }

        String serviceName = pathParts[1];
        List<ApiPermissionRule> apiPermissionRules = apiPermissionsRuleMap.get(serviceName);

        if (apiPermissionRules == null || apiPermissionRules.isEmpty()) {
            log.error("No permission rules found for service: {}", serviceName);
            return false;
        }

        String pathWithoutService = normalizedPath.substring(normalizedPath.indexOf("/", 1));

        for (ApiPermissionRule rule : apiPermissionRules) {
            if (rule.getPath() == null || rule.getPermissions() == null) {
                continue;
            }
            if (pathMatcher.match(rule.getPath(), pathWithoutService)) {
                if (rule.getPermissions().stream()
                        .anyMatch(p -> p.equalsIgnoreCase(BLOCKED_PERMISSION))) {
                    log.warn("Blocked access to internal-only path: {}", pathWithoutService);
                    return false;
                }
                if (rule.getPermissions().stream()
                        .anyMatch(p -> p.equalsIgnoreCase(ALL_PERMISSION) || userScopes.contains(p))) {
                    return true;
                }
            }
        }

        return true; //TODO: Default allow if no rules match will update later
    }

    private boolean isPublicUrl(String path) {
        return publicUrls.getUrls().stream().anyMatch(url -> pathMatcher.match(url, path));
    }

    private String extractJwtToken(String authorizationHeader) {
        String[] parts = authorizationHeader.split(SpecialChars.SPACE.getText());
        if (FilterValidationAndMapper.isArrayLengthNotOk(parts, CustomDataConfiguration.TOKEN_HEADER_ARRAY_LENGTH) ||
                FilterValidationAndMapper.isTokenPrefixNotOK(CustomDataConfiguration.TOKEN_PREFIX, parts)) {
            return null;
        }
        return (parts.length > 1 && parts[1] != null && !parts[1].isEmpty()) ? parts[1] : null;
    }

    private String encodeCurrentUserContext(CurrentUserContext context) {
        String json = JacksonUtil.objectToJson(context);
        if (json == null) {
            return null;
        }
        return CommonUtil.toBase64(json.getBytes(StandardCharsets.UTF_8));
    }
}
