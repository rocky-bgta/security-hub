package com.aspire.asat.phishing.service.impl.importer;

import lombok.Builder;
import lombok.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ImportBlockDetector {

    public DetectionResult detect(String html, String title, int statusCode) {
        String htmlLower = html == null ? "" : html.toLowerCase(Locale.ROOT);
        String titleLower = title == null ? "" : title.toLowerCase(Locale.ROOT);

        List<String> signals = new ArrayList<>();
        if (htmlLower.contains("cf-chl") || htmlLower.contains("cf-ray")
                || titleLower.contains("attention required")) {
            signals.add("cloudflare");
        }
        if (htmlLower.contains("akamai") || htmlLower.contains("akamai bot")
                || htmlLower.contains("datadome")) {
            signals.add("akamai_or_datadome");
        }
        if (htmlLower.contains("captcha") || htmlLower.contains("verify you are human")) {
            signals.add("captcha_or_human_check");
        }
        if (htmlLower.contains("access denied") || htmlLower.contains("forbidden")
                || titleLower.contains("access denied")) {
            signals.add("access_denied");
        }
        if (htmlLower.contains("just a moment") || htmlLower.contains("checking your browser")) {
            signals.add("challenge_wait_page");
        }

        boolean blocked = statusCode == 401 || statusCode == 403 || statusCode == 429 || !signals.isEmpty();
        String reason = null;
        if (blocked) {
            if (signals.stream().anyMatch(s -> s.contains("captcha") || s.contains("human_check"))) {
                reason = "bot_or_human_challenge";
            } else if (signals.stream().anyMatch(s -> s.contains("cloudflare") || s.contains("akamai"))) {
                reason = "waf_challenge";
            } else if (statusCode == 429) {
                reason = "rate_limited";
            } else if (statusCode == 401 || statusCode == 403) {
                reason = "access_denied";
            } else {
                reason = "challenge_detected";
            }
        }

        return DetectionResult.builder()
                .blocked(blocked)
                .reason(reason)
                .signals(signals)
                .build();
    }

    @Value
    @Builder
    public static class DetectionResult {
        boolean blocked;
        String reason;
        List<String> signals;
    }
}

