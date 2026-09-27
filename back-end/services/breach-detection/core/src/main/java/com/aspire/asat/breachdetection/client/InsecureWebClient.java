package com.aspire.asat.breachdetection.client;

import com.aspire.asat.breachdetection.dto.enums.BreachSeverity;
import com.aspire.asat.breachdetection.dto.enums.InsecureWebBreachStatus;
import com.aspire.asat.breachdetection.dto.request.InsecureWebBreachSearchRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InsecureWebClient {

    private static final int MAX_OUTBOUND_LOG_CHARS = 500;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private static final Set<String> WRAPPED_ARRAY_KEYS = Set.of("data", "results", "items", "breaches", "content");

    /**
     * InsecureWeb requires {@code api-version: v3} for {@code GET /api/dark-web/{id}/breaches} and
     * {@code POST /api/organizations} (organization create).
     */
    private static final String API_VERSION_HEADER = "api-version";
    private static final String API_VERSION_V3 = "v3";

    /** Log line only — real {@code api-key} is sent on the wire, never logged. */
    private static final String LOG_HEADERS_AUTH = "Accept=application/json, api-key=[REDACTED]";

    private static final String LOG_HEADERS_API_VERSION_V3 =
            "Accept=application/json, api-key=[REDACTED], api-version=v3";
    @Value("${insecureweb.api.key:}")
    private String apiKey;

    /**
     * Origin only (no path); paths include {@code /api/...}, e.g. {@code /api/dark-web/{id}/breaches}.
     */
    @Value("${insecureweb.api.url:https://app.insecureweb.com}")
    private String baseUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${insecureweb.api.skip-ssl-validation:false}")
    private boolean skipSslValidation;

    public InsecureWebClient(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                             @Value("${insecureweb.api.skip-ssl-validation:false}") boolean skipSslValidation) {
        this.skipSslValidation = skipSslValidation;
        if (skipSslValidation) {
            HttpClient insecureHttpClient = HttpClient.create()
                    .secure(sslContextSpec -> {
                        try {
                            sslContextSpec.sslContext(
                                    SslContextBuilder.forClient()
                                            .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                            .build()
                            );
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to initialize insecure SSL context", e);
                        }
                    });
            this.webClient = webClientBuilder
                    .clientConnector(new ReactorClientHttpConnector(insecureHttpClient))
                    .build();
            log.warn("InsecureWeb SSL validation is DISABLED (insecureweb.api.skip-ssl-validation=true). Use only for local/dev.");
        } else {
            this.webClient = webClientBuilder.build();
        }
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void normalizeBaseUrl() {
        if (!StringUtils.hasText(baseUrl)) {
            return;
        }
        String t = baseUrl.trim();
        while (t.endsWith("/")) {
            t = t.substring(0, t.length() - 1);
        }
        // Legacy configs used .../api as base; paths already start with /api/...
        if (t.endsWith("/api")) {
            t = t.substring(0, t.length() - 4);
        }
        baseUrl = t;
    }

    public ExternalOrganizationDto createOrganization(CreateOrganizationRequest request) {
        if (!isConfigured()) {
            throw new IllegalStateException("InsecureWeb API key not configured");
        }
        try {
            log.info("Creating organization in InsecureWeb: orgName={}, domains={}, emails={}",
                    request.getOrgName(),
                    request.getDomains(),
                    request.getEmails());
            JsonNode response = executePost("/api/organizations", request);
            return mapOrganization(response);
        } catch (Exception ex) {
            log.error("Error creating organization in InsecureWeb: {}", ex.getMessage(), ex);
            throw new RuntimeException("Failed to create organization in InsecureWeb: " + ex.getMessage(), ex);
        }

    }

    /**
     * Backward-compatible variant used by the sync flow (no filters beyond
     * {@code employeesOnly} / {@code maskData}).
     */
    public PagedBreachesResponse getOrganizationBreaches(long organizationId, boolean employeesOnly,
                                                         boolean maskData, int page, int size) {
        return searchOrganizationBreaches(organizationId, InsecureWebBreachSearchRequest.builder()
                .breachStatusIn(Set.of(InsecureWebBreachStatus.OPEN))
                .employeesOnly(employeesOnly)
                .maskData(maskData)
                .page(page)
                .size(size)
                .build());
    }

    /**
     * Filter-aware breach search against InsecureWeb. Emits the upstream's
     * operator-suffixed query syntax:
     * ?breachStatus.in=OPEN,IN_PROGRESS&domain.contains=example.com&email.contains=alice@
     * Empty/blank filters are omitted entirely.
     */
    public PagedBreachesResponse searchOrganizationBreaches(long organizationId,
                                                            InsecureWebBreachSearchRequest filter) {
        if (!isConfigured()) {
            return PagedBreachesResponse.empty(safePage(filter), safeSize(filter));
        }
        if (filter == null) {
            filter = InsecureWebBreachSearchRequest.builder().build();
        }
        InsecureWebBreachSearchRequest f = filter;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/api/dark-web/{organizationId}/breaches")
                .queryParam("page", safePage(f))
                .queryParam("size", safeSize(f))
                .queryParam("employeesOnly", f.getEmployeesOnly() == null ? Boolean.TRUE : f.getEmployeesOnly())
                .queryParam("maskData", f.getMaskData() == null ? Boolean.TRUE : f.getMaskData());

        if (f.getBreachStatusIn() != null && !f.getBreachStatusIn().isEmpty()) {
            String joined = f.getBreachStatusIn().stream()
                    .filter(Objects::nonNull)
                    .map(InsecureWebBreachStatus::name)
                    .collect(Collectors.joining(","));
            if (!joined.isEmpty()) {
                builder.queryParam("breachStatus.in", joined);
            }
        }

        if (StringUtils.hasText(f.getDomainContains())) {
            builder.queryParam("domain.contains", f.getDomainContains().trim());
        }
        if (StringUtils.hasText(f.getEmailContains())) {
            builder.queryParam("email.contains", f.getEmailContains().trim());
        }

        java.net.URI uri = builder.build(organizationId);
        JsonNode response = executeGetUriForDarkWebBreaches(uri);
        List<OrganizationBreachItem> items = extractNodes(response).stream()
                .map(this::mapOrganizationBreachItem)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        int totalPages = firstInt(response, safePage(f) + (items.isEmpty() ? 0 : 1), "totalPages", "pages");
        long totalElements = firstLong(response, (long) items.size(), "totalElements", "total");
        boolean last = firstBoolean(response, items.isEmpty(), "last");
        return PagedBreachesResponse.builder()
                .items(items)
                .page(safePage(f))
                .size(safeSize(f))
                .totalPages(totalPages)
                .totalElements(totalElements)
                .last(last)
                .build();
    }

    private int safePage(InsecureWebBreachSearchRequest filter) {
        return filter == null ? 0 : Math.max(filter.getPage(), 0);
    }

    private int safeSize(InsecureWebBreachSearchRequest filter) {
        int s = filter == null ? 20 : filter.getSize();
        if (s <= 0) {
            return 20;
        }
        return Math.min(s, 200);
    }


    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    /**
     * GET {@code /api/dark-web/.../breaches} — uses {@link #applyApiVersionV3Headers} (same {@code api-version: v3}
     * contract as organization create).
     */
    private JsonNode executeGetUriForDarkWebBreaches(java.net.URI uri) {
        return executeGetUri(uri, this::applyApiVersionV3Headers, LOG_HEADERS_API_VERSION_V3);
    }

    private JsonNode executeGetUri(
            java.net.URI uri,
            java.util.function.Consumer<HttpHeaders> headerConfigurer,
            String requestHeadersForLog) {
        try {
            return webClient.get()
                    .uri(uri)
                    .headers(headerConfigurer)
                    .exchangeToMono(response -> response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(body -> {
                                logOutboundInsecureWeb(
                                        "GET", uri.toString(), requestHeadersForLog, null, response.statusCode().value(), body);
                                if (response.statusCode().isError()) {
                                    throw new RuntimeException(
                                            "InsecureWeb request failed with status " + response.statusCode() + ", body: " + body);
                                }
                                return readJsonTree(body);
                            }))
                    .timeout(REQUEST_TIMEOUT)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("InsecureWeb API error: status " + ex.getStatusCode() +
                    ", body: " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to call InsecureWeb API: " + ex.getMessage(), ex);
        }
    }

    private JsonNode executePost(String path, Object requestBody) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + path).build().encode().toUri();
        String requestPayload = requestPayloadForLog(requestBody);
        try {
            return webClient.post()
                    .uri(uri)
                    .headers(this::applyApiVersionV3Headers)
                    .bodyValue(requestBody)
                    .exchangeToMono(response -> response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(body -> {
                                logOutboundInsecureWeb(
                                        "POST",
                                        uri.toString(),
                                        LOG_HEADERS_API_VERSION_V3,
                                        requestPayload,
                                        response.statusCode().value(),
                                        body);
                                if (response.statusCode().isError()) {
                                    throw new RuntimeException(
                                            "InsecureWeb request failed with status " + response.statusCode() + ", body: " + body);
                                }
                                return readJsonTree(body);
                            }))
                    .timeout(REQUEST_TIMEOUT)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("InsecureWeb API error: status " + ex.getStatusCode() +
                    ", body: " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to call InsecureWeb API: " + ex.getMessage(), ex);
        }
    }

    private void logOutboundInsecureWeb(
            String method,
            String url,
            String requestHeadersForLog,
            String requestPayload,
            int httpStatus,
            String responseBody) {
        log.info(
                "InsecureWeb outbound {} {}; requestHeaders=[{}]; requestPayload={}; httpStatus={}; responseBody={}",
                method,
                url,
                requestHeadersForLog,
                requestPayload == null ? "" : truncateForLog(requestPayload),
                httpStatus,
                truncateForLog(responseBody));
    }

    private String requestPayloadForLog(Object requestBody) {
        if (requestBody == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            return String.valueOf(requestBody);
        }
    }

    private JsonNode readJsonTree(String body) {
        if (body == null || body.isEmpty()) {
            return objectMapper.getNodeFactory().nullNode();
        }
        try {
            return objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("InsecureWeb response is not valid JSON", e);
        }
    }

    private static String truncateForLog(String s) {
        if (s == null) {
            return "";
        }
        if (s.length() <= MAX_OUTBOUND_LOG_CHARS) {
            return s;
        }
        return s.substring(0, MAX_OUTBOUND_LOG_CHARS) + "…[truncated; totalChars=" + s.length() + "]";
    }

    private void applyAuthHeaders(HttpHeaders headers) {
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("api-key", apiKey);
    }

    /** Auth plus {@code api-version: v3} for dark-web breach listing and organization create. */
    private void applyApiVersionV3Headers(HttpHeaders headers) {
        applyAuthHeaders(headers);
        headers.set(API_VERSION_HEADER, API_VERSION_V3);
    }

    private List<JsonNode> extractNodes(JsonNode response) {
        if (response == null || response.isNull()) {
            return List.of();
        }
        if (response.isArray()) {
            return toNodeList(response);
        }
        for (String key : WRAPPED_ARRAY_KEYS) {
            JsonNode wrapped = response.get(key);
            if (wrapped != null && wrapped.isArray()) {
                return toNodeList(wrapped);
            }
        }
        for (String key : WRAPPED_ARRAY_KEYS) {
            JsonNode wrapped = response.get(key);
            if (wrapped != null && wrapped.isObject()) {
                return List.of(wrapped);
            }
        }
        return response.isObject() ? List.of(response) : List.of();
    }

    private List<JsonNode> toNodeList(JsonNode arrayNode) {
        List<JsonNode> nodes = new ArrayList<>();
        arrayNode.forEach(nodes::add);
        return nodes;
    }

    private OrganizationBreachItem mapOrganizationBreachItem(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return OrganizationBreachItem.builder()
                .id(firstText(node, "id"))
                .timestamp(parseInstant(firstText(node, "timestamp", "createdAt", "date")))
                .domain(firstText(node, "domain"))
                .email(normalizeNullableEmail(firstText(node, "email")))
                .ipAddress(firstText(node, "ipAddress"))
                .username(firstText(node, "username"))
                .password(firstText(node, "password"))
                .hashedPassword(firstText(node, "hashedPassword"))
                .phone(firstText(node, "phone"))
                .databaseName(firstText(node, "databaseName"))
                .foundIn(firstText(node, "foundIn"))
                .source(firstText(node, "source"))
                .leakName(firstText(node, "leakName"))
                .breachDescription(firstText(node, "breachDescription", "description"))
                .compromisedData(firstText(node, "compromisedData"))
                .victimDomain(firstText(node, "victimDomain"))
                .organizationId(firstLong(node, null, "organizationId"))
                .organizationElement(firstText(node, "organizationElement"))
                .organizationElementType(firstText(node, "organizationElementType"))
                .breachStatus(InsecureWebBreachStatus.fromExternal(firstText(node, "breachStatus")))
                .employee(firstBoolean(node, null, "employee"))
                .echoesCount(firstInt(node, null, "echoesCount"))
                .rawJson(node.toString())
                .build();
    }

    private String firstText(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value != null && !value.isNull() && StringUtils.hasText(value.asText())) {
                return value.asText();
            }
        }
        return null;
    }

    private Integer firstInt(JsonNode node, Integer defaultValue, String... keys) {
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value == null || value.isNull()) {
                continue;
            }
            if (value.isInt() || value.isLong()) {
                return value.asInt();
            }
            if (value.isTextual()) {
                try {
                    return Integer.parseInt(value.asText());
                } catch (NumberFormatException ignored) {
                    // continue fallback scan
                }
            }
        }
        return defaultValue;
    }

    private Long firstLong(JsonNode node, Long defaultValue, String... keys) {
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value == null || value.isNull()) {
                continue;
            }
            if (value.isLong() || value.isInt()) {
                return value.asLong();
            }
            if (value.isTextual()) {
                try {
                    return Long.parseLong(value.asText());
                } catch (NumberFormatException ignored) {
                    // continue fallback scan
                }
            }
        }
        return defaultValue;
    }

    private Boolean firstBoolean(JsonNode node, Boolean defaultValue, String... keys) {
        if (node == null || node.isNull()) {
            return defaultValue;
        }
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value == null || value.isNull()) {
                continue;
            }
            if (value.isBoolean()) {
                return value.asBoolean();
            }
            if (value.isTextual()) {
                return Boolean.parseBoolean(value.asText());
            }
        }
        return defaultValue;
    }

    private Instant parseInstant(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (Exception ignored) {
            try {
                return objectMapper.convertValue(value, Instant.class);
            } catch (Exception conversionIgnored) {
                return null;
            }
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullableEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        return normalizeEmail(email);
    }

    private ExternalOrganizationDto mapOrganization(JsonNode response) {
        if (response == null || response.isNull()) {
            throw new IllegalStateException("InsecureWeb organization response is empty");
        }
        JsonNode node = response;
        if (response.has("data") && response.get("data").isObject()) {
            node = response.get("data");
        }
        Long id = firstLong(node, null, "id");
        if (id == null) {
            throw new IllegalStateException("InsecureWeb organization id missing from response");
        }
        return ExternalOrganizationDto.builder()
                .id(id)
                .orgName(firstText(node, "orgName"))
                .orgDescription(firstText(node, "orgDescription"))
                .domains(extractStringList(node, "domains"))
                .emails(extractStringList(node, "emails"))
                .users(extractStringList(node, "users"))
                .ips(extractStringList(node, "ips"))
                .phones(extractStringList(node, "phones"))
                .scanServices(extractStringList(node, "scanServices"))
                .build();
    }

    private List<String> extractStringList(JsonNode node, String... keys) {
        if (node == null || node.isNull()) {
            return List.of();
        }
        for (String key : keys) {
            JsonNode value = node.get(key);
            if (value == null || value.isNull()) {
                continue;
            }
            if (value.isArray()) {
                List<String> items = new ArrayList<>();
                value.forEach(entry -> {
                    if (entry != null && !entry.isNull()) {
                        String asText = entry.asText();
                        if (StringUtils.hasText(asText)) {
                            items.add(asText.trim());
                        }
                    }
                });
                return items;
            }
        }
        return List.of();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExternalRecipientBreachDto {
        private String externalId;
        private String breachName;
        private String email;
        private String firstName;
        private String lastName;
        private List<String> tags;
        private int breachCount;
        private String domain;
        private Instant dateOfBreach;
        private String description;
        private List<String> compromisedData;
        private String severity;

        public BreachSeverity getSeverityEnum() {
            if (severity == null) {
                return BreachSeverity.LOW;
            }
            switch (severity.toUpperCase(Locale.ROOT)) {
                case "CRITICAL":
                    return BreachSeverity.CRITICAL;
                case "HIGH":
                    return BreachSeverity.HIGH;
                case "MEDIUM":
                    return BreachSeverity.MEDIUM;
                default:
                    return BreachSeverity.LOW;
            }
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateOrganizationRequest {
        private String orgName;
        private String orgDescription;
        private List<String> domains;
        private List<String> emails;
        private List<String> users;
        private List<String> ips;
        private List<String> phones;
        private List<String> scanServices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExternalOrganizationDto {
        private Long id;
        private String orgName;
        private String orgDescription;
        private List<String> domains;
        private List<String> emails;
        private List<String> users;
        private List<String> ips;
        private List<String> phones;
        private List<String> scanServices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrganizationBreachItem {
        private String id;
        private Instant timestamp;
        private String domain;
        private String email;
        private String ipAddress;
        private String username;
        private String password;
        private String hashedPassword;
        private String phone;
        private String databaseName;
        private String foundIn;
        private String source;
        private String leakName;
        private String breachDescription;
        private String compromisedData;
        private String victimDomain;
        private Long organizationId;
        private String organizationElement;
        private String organizationElementType;
        private InsecureWebBreachStatus breachStatus;
        private Boolean employee;
        private Integer echoesCount;
        private String rawJson;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagedBreachesResponse {
        @Builder.Default
        private List<OrganizationBreachItem> items = new ArrayList<>();
        private int page;
        private int size;
        private int totalPages;
        private long totalElements;
        private boolean last;

        public static PagedBreachesResponse empty(int page, int size) {
            return PagedBreachesResponse.builder()
                    .items(List.of())
                    .page(page)
                    .size(size)
                    .totalPages(0)
                    .totalElements(0)
                    .last(true)
                    .build();
        }
    }
}
