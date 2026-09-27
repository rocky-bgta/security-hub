package com.aspire.asat.cms.service.geoip;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@Slf4j
public class GeoIpServiceImpl implements GeoIpService {

    private static final String DEFAULT_LANGUAGE = "en";
    private static final String IP_API_URL = "http://ip-api.com/json/%s?fields=countryCode";

    /**
     * Country code (ISO 3166-1 alpha-2) to language code (ISO 639-1) mapping.
     * Unmapped countries default to "en".
     */
    private static final Map<String, String> COUNTRY_TO_LANGUAGE = Map.ofEntries(
            Map.entry("BD", "bn"),  // Bangladesh
            Map.entry("IN", "hi"),  // India (Hindi)
            Map.entry("PK", "ur"),  // Pakistan (Urdu)
            Map.entry("ES", "es"),  // Spain
            Map.entry("FR", "fr"),  // France
            Map.entry("DE", "de"),  // Germany
            Map.entry("IT", "it"),  // Italy
            Map.entry("PT", "pt"),  // Portugal
            Map.entry("RU", "ru"),  // Russia
            Map.entry("CN", "zh"),  // China
            Map.entry("JP", "ja"),  // Japan
            Map.entry("AR", "ar"),  // Saudi Arabia / Arabic
            Map.entry("SA", "ar"),
            Map.entry("EG", "ar"),
            Map.entry("US", "en"),
            Map.entry("GB", "en"),
            Map.entry("AU", "en"),
            Map.entry("CA", "en"),
            Map.entry("NZ", "en")
    );

    private final WebClient webClient;

    public GeoIpServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String getLanguageFromIp(String clientIp) {

        return DEFAULT_LANGUAGE;

//        if (clientIp == null || clientIp.isBlank()) {
//            return DEFAULT_LANGUAGE;
//        }
//        String ip = clientIp.trim();
//        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
//            return DEFAULT_LANGUAGE;
//        }
//        try {
//            String url = String.format(IP_API_URL, ip);
//            JsonNode response = webClient.get()
//                    .uri(url)
//                    .retrieve()
//                    .bodyToMono(JsonNode.class)
//                    .block();
//            if (response != null && response.has("countryCode")) {
//                String countryCode = response.get("countryCode").asText();
//                String language = COUNTRY_TO_LANGUAGE.getOrDefault(countryCode, DEFAULT_LANGUAGE);
//                log.debug("GeoIP: ip={} -> country={} -> language={}", ip, countryCode, language);
//                return language;
//            }
//        } catch (Exception e) {
//            log.warn("GeoIP lookup failed for ip={}: {}", ip, e.getMessage());
//        }
//        return DEFAULT_LANGUAGE;
    }
}
