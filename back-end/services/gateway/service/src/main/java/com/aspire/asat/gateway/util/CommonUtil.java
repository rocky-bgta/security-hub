package com.aspire.asat.gateway.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.Base64;

public class CommonUtil {
    private CommonUtil() {
    }

    private static final String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };


    public static String getClientRealIpAddress(ServerHttpRequest request) {
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeaders().getFirst(header);
            if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) return ip;
        }
        return "127.0.0.1";
    }


    public static String toBase64(byte[] byteArray) {
        return Base64.getUrlEncoder().encodeToString(byteArray);
    }

}
