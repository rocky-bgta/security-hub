package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.config.SubmissionAwarenessProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;
/**
 * Renders the HTML interstitial shown after a landing page form submission.
 */
@Component
@RequiredArgsConstructor
public class SubmissionAwarenessPageRenderer {

    private final SubmissionAwarenessProperties properties;
    private final RedirectUrlSanitizer redirectUrlSanitizer;

    public String render(String redirectUrl) {
        int delaySeconds = properties.getEffectiveRedirectDelaySeconds();
        return render(redirectUrlSanitizer.sanitize(redirectUrl), delaySeconds);
    }

    String render(String safeRedirectUrl, int delaySeconds) {
        int clampedDelay = Math.min(30, Math.max(1, delaySeconds));
        int delayMs = clampedDelay * 1000;
        String jsRedirectUrl = toJsonString(safeRedirectUrl);
        String metaRefreshUrl = encodeForMetaRefresh(safeRedirectUrl);
        String guideUrl = redirectUrlSanitizer.sanitizeOptional(properties.getGuideUrl());
        String guideLinkHtml = guideUrl != null
                ? "<p><a href=\"" + HtmlUtils.htmlEscape(guideUrl)
                + "\" target=\"_blank\" rel=\"noopener noreferrer\">Review our phishing awareness guide</a>"
                + " to strengthen your defenses.</p>"
                : "<p>Review our phishing awareness guide to strengthen your defenses.</p>";

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <title>Security Awareness</title>
                  <meta http-equiv="refresh" content="%d;url=%s">
                  <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                           display: flex; justify-content: center; align-items: center;
                           min-height: 100vh; margin: 0; background: #f5f5f5; color: #333; }
                    .card { background: #fff; border-radius: 12px; padding: 48px; text-align: left;
                            box-shadow: 0 2px 12px rgba(0,0,0,0.08); max-width: 560px; }
                    h1 { color: #dc2626; margin-top: 0; margin-bottom: 16px; font-size: 1.5rem; }
                    p  { line-height: 1.6; color: #555; margin: 0 0 12px; }
                    .countdown { margin-top: 20px; font-size: 0.95rem; color: #666; }
                    a { color: #2563eb; }
                  </style>
                </head>
                <body>
                  <div class="card">
                    <h1>Alert</h1>
                    <p>You just entered sensitive information into a simulated phishing page.</p>
                    <p>In a real attack, this could have allowed criminals to access your accounts.</p>
                    <p>Remember: Never share OTPs or account details on suspicious websites.</p>
                    %s
                    <p class="countdown">Redirecting in <span id="countdown">%d</span> seconds...</p>
                  </div>
                  <script>
                    (function() {
                      var redirectUrl = %s;
                      var seconds = %d;
                      var countdownEl = document.getElementById('countdown');
                      var timer = setInterval(function() {
                        seconds--;
                        if (countdownEl) {
                          countdownEl.textContent = String(seconds);
                        }
                        if (seconds <= 0) {
                          clearInterval(timer);
                        }
                      }, 1000);
                      setTimeout(function() {
                        window.location.replace(redirectUrl);
                      }, %d);
                    })();
                  </script>
                </body>
                </html>
                """.formatted(
                clampedDelay,
                metaRefreshUrl,
                guideLinkHtml,
                clampedDelay,
                jsRedirectUrl,
                clampedDelay,
                delayMs
        );
    }

    private String toJsonString(String value) {
        if (value == null) {
            return "\"/\"";
        }
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }

    private String encodeForMetaRefresh(String url) {
        return HtmlUtils.htmlEscape(url);
    }
}
