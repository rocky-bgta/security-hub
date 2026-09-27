package com.aspire.asat.phishing.util;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Normalizes deepfake media references that may be either an S3 object key or a full
 * (possibly presigned) URL. Callers must store and pass object keys; this utility
 * recovers the key when a URL was supplied by mistake.
 */
public final class DeepfakeS3KeyUtils {

    private DeepfakeS3KeyUtils() {
    }

    /**
     * @return the S3 object key, or {@code null}/{@code blank} unchanged
     */
    public static String normalizeKey(String keyOrUrl) {
        return normalizeKey(keyOrUrl, null);
    }

    /**
     * @param bucket optional bucket name used to strip path-style {@code /bucket/key} URLs
     * @return the S3 object key, or {@code null}/{@code blank} unchanged
     */
    public static String normalizeKey(String keyOrUrl, String bucket) {
        if (keyOrUrl == null || keyOrUrl.isBlank()) {
            return keyOrUrl;
        }

        String candidate = keyOrUrl.trim();
        for (int i = 0; i < 3 && looksLikeHttpUrl(candidate); i++) {
            String extracted = extractKeyFromUrl(candidate, bucket);
            if (extracted == null || extracted.equals(candidate)) {
                break;
            }
            candidate = extracted;
        }

        candidate = trimLeadingSlash(candidate);
        if (bucket != null && !bucket.isBlank() && candidate.startsWith(bucket + "/")) {
            candidate = candidate.substring(bucket.length() + 1);
        }
        return candidate;
    }

    public static boolean looksLikeHttpUrl(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        return trimmed.startsWith("http://")
                || trimmed.startsWith("https://")
                || trimmed.startsWith("http%3A")
                || trimmed.startsWith("https%3A")
                || trimmed.startsWith("http%3a")
                || trimmed.startsWith("https%3a");
    }

    private static String extractKeyFromUrl(String url, String bucket) {
        String decodedOnce = url;
        if (url.contains("%")) {
            try {
                decodedOnce = URLDecoder.decode(url, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException ignored) {
                decodedOnce = url;
            }
        }

        try {
            URI uri = URI.create(decodedOnce.contains("://") ? decodedOnce : "https://" + decodedOnce);
            String path = uri.getPath();
            if (path == null || path.isBlank() || "/".equals(path)) {
                return null;
            }
            String key = trimLeadingSlash(path);
            if (bucket != null && !bucket.isBlank() && key.startsWith(bucket + "/")) {
                key = key.substring(bucket.length() + 1);
            }
            // Path may itself be a nested URL (double-wrapped key).
            if (looksLikeHttpUrl(key) || key.startsWith("http:/") || key.startsWith("https:/")) {
                String nested = key.startsWith("http:/") && !key.startsWith("http://")
                        ? key.replaceFirst("^http:/", "http://")
                        : key;
                nested = nested.startsWith("https:/") && !nested.startsWith("https://")
                        ? nested.replaceFirst("^https:/", "https://")
                        : nested;
                if (uri.getRawQuery() != null && !nested.contains("?")) {
                    nested = nested + "?" + uri.getRawQuery();
                }
                return nested;
            }
            return key;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String trimLeadingSlash(String value) {
        if (value == null) {
            return null;
        }
        int i = 0;
        while (i < value.length() && value.charAt(i) == '/') {
            i++;
        }
        return i == 0 ? value : value.substring(i);
    }
}
