package com.aspire.asat.breachdetection.client;

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
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Minimal client for the Shodan REST API.
 *
 * Covers two endpoints we need for IP + Domain breach monitoring:
 *   - {@code GET /shodan/host/{ip}?key=...}  (Search Methods)
 *   - {@code GET /dns/domain/{domain}?key=...} (DNS Methods)
 *
 * All requests authenticate via the {@code key} query parameter (Shodan does
 * not support Authorization headers).
 */
@Component
@Slf4j
public class ShodanClient {

    private static final int MAX_OUTBOUND_LOG_CHARS = 500;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(45);

    @Value("${shodan.api.key:}")
    private String apiKey;

    @Value("${shodan.api.url:https://api.shodan.io}")
    private String baseUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public ShodanClient(
            WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper,
            @Value("${shodan.api.skip-ssl-validation:false}") boolean skipSslValidation) {
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
                            throw new RuntimeException("Failed to initialize insecure SSL context for Shodan", e);
                        }
                    });
            this.webClient = webClientBuilder
                    .clientConnector(new ReactorClientHttpConnector(insecureHttpClient))
                    .build();
            log.warn("Shodan SSL validation is DISABLED (shodan.api.skip-ssl-validation=true). Use only for local/dev.");
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
        baseUrl = t;
    }

    public boolean isConfigured() {
        return StringUtils.hasText(apiKey);
    }

    /** {@code GET /shodan/host/{ip}} — returns host services, vulns, tags. */
    public HostInfo getHostInformation(String ip) {
        if (!isConfigured()) {
            throw new IllegalStateException("Shodan API key not configured");
        }
        JsonNode node = executeGet("/shodan/host/{ip}?key={key}&minify=false", ip, apiKey);
        return mapHostInfo(node);
    }

    /** {@code GET /dns/domain/{domain}} — returns subdomains and DNS records. */
    public DomainInfo getDomainInformation(String domain) {
        if (!isConfigured()) {
            throw new IllegalStateException("Shodan API key not configured");
        }
        JsonNode node = executeGet("/dns/domain/{domain}?key={key}", domain, apiKey);
        return mapDomainInfo(node);
    }

    private JsonNode executeGet(String pathTemplate, Object... uriVariables) {
        URI uri = UriComponentsBuilder.fromUriString(baseUrl + pathTemplate)
                .buildAndExpand(uriVariables)
                .encode()
                .toUri();
        String urlForLog = redactShodanKeyQuery(uri.toString());
        try {
            return webClient.get()
                    .uri(uri)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchangeToMono(response -> response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(body -> {
                                logOutboundShodan(urlForLog, response.statusCode().value(), body);
                                if (response.statusCode().isError()) {
                                    throw new RuntimeException(
                                            "Shodan request failed with status " + response.statusCode() + ", body: " + body);
                                }
                                return readJsonTree(body);
                            }))
                    .timeout(REQUEST_TIMEOUT)
                    .block();
        } catch (WebClientResponseException ex) {
            throw new RuntimeException("Shodan API error: status " + ex.getStatusCode() +
                    ", body: " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to call Shodan API: " + ex.getMessage(), ex);
        }
    }

    private void logOutboundShodan(String urlWithoutSecrets, int httpStatus, String responseBody) {
        log.info(
                "Shodan outbound GET {}; requestPayload=(none); httpStatus={}; responseBody={}",
                urlWithoutSecrets,
                httpStatus,
                truncateForLog(responseBody));
    }

    /** Shodan uses the {@code key} query parameter; never log the raw value. */
    private static String redactShodanKeyQuery(String url) {
        if (url == null) {
            return "";
        }
        return url.replaceAll("([?&])key=[^&]*", "$1key=[REDACTED]");
    }

    private JsonNode readJsonTree(String body) {
        if (body == null || body.isEmpty()) {
            return objectMapper.getNodeFactory().nullNode();
        }
        try {
            return objectMapper.readTree(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Shodan response is not valid JSON", e);
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

    private HostInfo mapHostInfo(JsonNode node) {
        if (node == null || node.isNull()) {
            return HostInfo.empty();
        }
        List<String> hostnames = stringList(node.get("hostnames"));
        List<String> domains = stringList(node.get("domains"));
        List<Integer> ports = intList(node.get("ports"));
        List<String> tags = stringList(node.get("tags"));
        List<String> vulns = stringList(node.get("vulns"));
        List<HostService> services = new ArrayList<>();
        JsonNode data = node.get("data");
        if (data != null && data.isArray()) {
            for (JsonNode svc : data) {
                services.add(HostService.builder()
                        .port(svc.hasNonNull("port") ? svc.get("port").asInt() : null)
                        .transport(svc.hasNonNull("transport") ? svc.get("transport").asText() : null)
                        .product(svc.hasNonNull("product") ? svc.get("product").asText() : null)
                        .hostnames(stringList(svc.get("hostnames")))
                        .tags(stringList(svc.get("tags")))
                        .vulns(stringList(svc.get("vulns")))
                        .build());
            }
        }
        return HostInfo.builder()
                .ip(node.hasNonNull("ip_str") ? node.get("ip_str").asText() : null)
                .org(node.hasNonNull("org") ? node.get("org").asText() : null)
                .isp(node.hasNonNull("isp") ? node.get("isp").asText() : null)
                .countryCode(node.hasNonNull("country_code") ? node.get("country_code").asText() : null)
                .lastUpdate(node.hasNonNull("last_update") ? node.get("last_update").asText() : null)
                .hostnames(hostnames)
                .domains(domains)
                .ports(ports)
                .tags(tags)
                .vulns(vulns)
                .services(services)
                .build();
    }

    private DomainInfo mapDomainInfo(JsonNode node) {
        if (node == null || node.isNull()) {
            return DomainInfo.empty();
        }
        List<DnsRecord> records = new ArrayList<>();
        JsonNode data = node.get("data");
        if (data != null && data.isArray()) {
            for (JsonNode rec : data) {
                records.add(DnsRecord.builder()
                        .subdomain(rec.hasNonNull("subdomain") ? rec.get("subdomain").asText() : null)
                        .type(rec.hasNonNull("type") ? rec.get("type").asText() : null)
                        .value(rec.hasNonNull("value") ? rec.get("value").asText() : null)
                        .lastSeen(rec.hasNonNull("last_seen") ? rec.get("last_seen").asText() : null)
                        .build());
            }
        }
        return DomainInfo.builder()
                .domain(node.hasNonNull("domain") ? node.get("domain").asText() : null)
                .tags(stringList(node.get("tags")))
                .subdomains(stringList(node.get("subdomains")))
                .records(records)
                .build();
    }

    private List<String> stringList(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(n -> {
                if (n != null && !n.isNull()) {
                    out.add(n.asText());
                }
            });
        }
        return out;
    }

    private List<Integer> intList(JsonNode node) {
        List<Integer> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(n -> {
                if (n != null && !n.isNull() && n.canConvertToInt()) {
                    out.add(n.asInt());
                }
            });
        }
        return out;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HostInfo {
        private String ip;
        private String org;
        private String isp;
        private String countryCode;
        private String lastUpdate;
        private List<String> hostnames;
        private List<String> domains;
        private List<Integer> ports;
        private List<String> tags;
        private List<String> vulns;
        private List<HostService> services;

        public static HostInfo empty() {
            return HostInfo.builder()
                    .hostnames(List.of())
                    .domains(List.of())
                    .ports(List.of())
                    .tags(List.of())
                    .vulns(List.of())
                    .services(List.of())
                    .build();
        }

        public List<String> getSafeTags() {
            return tags == null ? List.of() : tags;
        }

        public List<String> getSafeVulns() {
            return vulns == null ? List.of() : vulns;
        }

        public List<HostService> getSafeServices() {
            return services == null ? List.of() : services;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HostService {
        private Integer port;
        private String transport;
        private String product;
        private List<String> hostnames;
        private List<String> tags;
        private List<String> vulns;

        public List<String> getSafeTags() {
            return tags == null ? List.of() : tags;
        }

        public List<String> getSafeVulns() {
            return vulns == null ? List.of() : vulns;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DomainInfo {
        private String domain;
        private List<String> tags;
        private List<String> subdomains;
        private List<DnsRecord> records;

        public static DomainInfo empty() {
            return DomainInfo.builder()
                    .tags(List.of())
                    .subdomains(List.of())
                    .records(List.of())
                    .build();
        }

        public List<String> getSafeTags() {
            return tags == null ? List.of() : tags;
        }

        public List<DnsRecord> getSafeRecords() {
            return records == null ? List.of() : records;
        }

        public List<String> getSafeSubdomains() {
            return subdomains == null ? List.of() : subdomains;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DnsRecord {
        private String subdomain;
        private String type;
        private String value;
        private String lastSeen;

        public boolean isEmail() {
            return Objects.equals(type, "MX");
        }
    }
}
