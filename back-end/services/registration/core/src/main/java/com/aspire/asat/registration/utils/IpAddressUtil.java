package com.aspire.asat.registration.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public class IpAddressUtil {

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED"
    };

    private IpAddressUtil() {
        // Utility class - prevent instantiation
    }

    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        try {
            for (String header : IP_HEADER_CANDIDATES) {
                String ipAddress = request.getHeader(header);
                if (StringUtils.hasText(ipAddress) && !"unknown".equalsIgnoreCase(ipAddress)) {
                    // X-Forwarded-For can contain multiple IPs, take the first one
                    if (ipAddress.contains(",")) {
                        return ipAddress.split(",")[0].trim();
                    }
                    return ipAddress;
                }
            }
            return request.getRemoteAddr();
        } catch (Exception ex) {
            return null;
        }
    }
}