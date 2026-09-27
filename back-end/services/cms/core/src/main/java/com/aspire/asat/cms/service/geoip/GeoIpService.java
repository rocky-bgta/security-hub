package com.aspire.asat.cms.service.geoip;

/**
 * Resolves language code from client IP using geolocation.
 */
public interface GeoIpService {

    /**
     * Resolves language code from client IP.
     * Uses X-Forwarded-For, X-Real-IP, or RemoteAddr.
     *
     * @param clientIp Client IP address. If null or empty, returns default "en".
     * @return Language code (e.g., en, bn, hi). Default "en" if IP/country unknown.
     */
    String getLanguageFromIp(String clientIp);
}
