package com.aspire.asat.phishing.service.impl.importer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BrowserCaptureClient {
    private final ObjectMapper objectMapper;

    @Value("${landing.import.browser-service.enabled:false}")
    private boolean enabled;

    @Value("${landing.import.browser-service.url:http://127.0.0.1:3002/clone}")
    private String browserServiceUrl;

    @Value("${landing.import.browser-service.timeout-ms:30000}")
    private int timeoutMs;

    public BrowserCaptureResult capture(String url, boolean injectBase, boolean trackOriginalUrl) {
        if (!enabled) {
            return BrowserCaptureResult.disabled();
        }
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(Math.max(1000, timeoutMs)))
                    .build();
            String body = objectMapper.writeValueAsString(Map.of(
                    "url", url,
                    "settings", Map.of(
                            "injectBase", injectBase,
                            "trackUrl", trackOriginalUrl,
                            "stripJs", false,
                            "removeRedirects", false
                    )
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(browserServiceUrl))
                    .timeout(Duration.ofMillis(Math.max(1000, timeoutMs)))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Browser capture service returned non-2xx: {}", response.statusCode());
                return BrowserCaptureResult.failed("browser_service_non_2xx_" + response.statusCode());
            }

            BrowserResponse payload = objectMapper.readValue(response.body(), BrowserResponse.class);
            if (!payload.success) {
                return BrowserCaptureResult.failed(payload.error != null ? payload.error : "browser_capture_failed");
            }
            if (payload.html == null || payload.html.isBlank()) {
                return BrowserCaptureResult.failed("browser_capture_empty_html");
            }

            return BrowserCaptureResult.success(payload.html, payload.url, payload.title);
        } catch (Exception e) {
            log.warn("Browser capture failed: {}", e.getMessage());
            return BrowserCaptureResult.failed("browser_capture_unavailable");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class BrowserResponse {
        public boolean success;
        public String html;
        public String title;
        public String url;
        public String error;
    }

    public record BrowserCaptureResult(boolean enabled, boolean success, String html, String finalUrl,
                                       String title, String errorReason) {
        static BrowserCaptureResult disabled() {
            return new BrowserCaptureResult(false, false, null, null, null, "disabled");
        }

        static BrowserCaptureResult failed(String reason) {
            return new BrowserCaptureResult(true, false, null, null, null, reason);
        }

        static BrowserCaptureResult success(String html, String finalUrl, String title) {
            return new BrowserCaptureResult(true, true, html, finalUrl, title, null);
        }
    }
}

