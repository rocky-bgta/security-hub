package com.aspire.asat.cms.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extracts client IP from HTTP request.
 * Checks X-Forwarded-For (first IP if comma-separated), X-Real-IP, then RemoteAddr.
 */
public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            String first = comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
            if (!first.isEmpty()) {
                return first;
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr : null;
    }
}
