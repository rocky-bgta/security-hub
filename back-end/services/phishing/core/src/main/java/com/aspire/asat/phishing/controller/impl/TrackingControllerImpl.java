package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.controller.TrackingController;
import com.aspire.asat.phishing.service.TrackingService;
import com.aspire.asat.phishing.service.UrlShortenerService;
import com.aspire.asat.phishing.service.support.RedirectUrlSanitizer;
import com.aspire.asat.phishing.service.support.SubmissionAwarenessPageRenderer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TrackingControllerImpl implements TrackingController {

    private static final byte[] TRANSPARENT_1X1_GIF = Base64.getDecoder().decode(
            "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7"
    );

    private static final String REPORT_CONFIRMATION_HTML = """
            <!DOCTYPE html>
            <html lang="en">
            <head><meta charset="UTF-8"><title>Report Received</title>
            <style>
              body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                     display: flex; justify-content: center; align-items: center;
                     min-height: 100vh; margin: 0; background: #f5f5f5; color: #333; }
              .card { background: #fff; border-radius: 12px; padding: 48px; text-align: center;
                      box-shadow: 0 2px 12px rgba(0,0,0,0.08); max-width: 480px; }
              h1 { color: #16a34a; margin-bottom: 8px; }
              p  { line-height: 1.6; color: #555; }
            </style></head>
            <body><div class="card">
              <h1>Thank You!</h1>
              <p>Your report has been recorded. This was a simulated phishing exercise
                 conducted by your organization's security team.</p>
              <p>Great job identifying the phishing email!</p>
            </div></body></html>
            """;

    private final TrackingService trackingService;
    private final SubmissionAwarenessPageRenderer submissionAwarenessPageRenderer;
    private final UrlShortenerService urlShortenerService;
    private final RedirectUrlSanitizer redirectUrlSanitizer;

    @Override
    public ResponseEntity<byte[]> trackOpen(String trackingId, HttpServletRequest request) {
        try {
            trackingService.recordOpen(trackingId, userAgent(request), clientIp(request));
        } catch (Exception e) {
            log.error("Error recording open for trackingId={}: {}", trackingId, e.getMessage());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_GIF)
                .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate")
                .header(HttpHeaders.PRAGMA, "no-cache")
                .body(TRANSPARENT_1X1_GIF);
    }

    @Override
    public ResponseEntity<Void> trackClick(String trackingId, String url, HttpServletRequest request) {
        try {
            trackingService.recordClick(trackingId, userAgent(request), clientIp(request));
        } catch (Exception e) {
            log.error("Error recording click for trackingId={}: {}", trackingId, e.getMessage());
        }

        String decodedUrl = safeDecodeUrl(url);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, decodedUrl)
                .build();
    }

    @Override
    public ResponseEntity<String> trackPhish(String trackingId, HttpServletRequest request) {
        try {
            String html = trackingService.serveLandingPage(trackingId, userAgent(request), clientIp(request));
            if (html != null) {
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .body(html);
            }
        } catch (Exception e) {
            log.error("Error serving landing page for trackingId={}: {}", trackingId, e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.TEXT_HTML)
                .body("<html><body><h1>Page Not Found</h1></body></html>");
    }

    @Override
    public ResponseEntity<String> redirectShortUrl(String shortCode) {
        String originalUrl = urlShortenerService.resolve(shortCode)
                .map(redirectUrlSanitizer::sanitizeOptional)
                .orElse(null);
        if (originalUrl != null && !originalUrl.isBlank()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, originalUrl)
                    .build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.TEXT_HTML)
                .body("<html><body><h1>Page Not Found</h1></body></html>");
    }

    @Override
    public ResponseEntity<String> trackSubmit(String trackingId, Map<String, String> formData, HttpServletRequest request) {
        String redirectUrl = "/";
        try {
            String resolved = trackingService.recordSubmission(
                    trackingId, formData, userAgent(request), clientIp(request));
            if (resolved != null && !resolved.isBlank()) {
                redirectUrl = resolved;
            }
        } catch (Exception e) {
            log.error("Error recording submission for trackingId={}: {}", trackingId, e.getMessage());
        }

        String html = submissionAwarenessPageRenderer.render(redirectUrl);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @Override
    public ResponseEntity<String> trackReport(String trackingId, HttpServletRequest request) {
        try {
            trackingService.recordReport(trackingId, userAgent(request), clientIp(request));
        } catch (Exception e) {
            log.error("Error recording report for trackingId={}: {}", trackingId, e.getMessage());
        }

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(REPORT_CONFIRMATION_HTML);
    }

    @Override
    public ResponseEntity<Map<String, Object>> trackReportFromAddIn(
            String trackingId,
            Map<String, Object> payload,
            HttpServletRequest request
    ) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            if (payload != null) {
                metadata.putAll(payload);
            }
            metadata.putIfAbsent("source", "outlook-addin");
            metadata.putIfAbsent("requestPath", request.getRequestURI());

            trackingService.recordReport(trackingId, userAgent(request), clientIp(request), metadata);
        } catch (Exception e) {
            log.error("Error recording add-in report for trackingId={}: {}", trackingId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("success", false, "message", "Failed to record report"));
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "success", true,
                        "trackingId", trackingId,
                        "message", "Report recorded successfully"
                ));
    }

    // --- helpers ---

    private String userAgent(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.USER_AGENT);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String safeDecodeUrl(String url) {
        if (url == null || url.isBlank()) {
            return "/";
        }
        try {
            String decoded = URLDecoder.decode(url, StandardCharsets.UTF_8);
            if (decoded.startsWith("http://") || decoded.startsWith("https://")) {
                return decoded;
            }
        } catch (Exception e) {
            log.warn("Failed to decode URL: {}", url);
        }
        return "/";
    }
}
