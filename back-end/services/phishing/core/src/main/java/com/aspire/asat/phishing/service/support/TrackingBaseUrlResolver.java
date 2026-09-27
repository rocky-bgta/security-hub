package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.enums.DomainStatus;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.model.Domain;
import com.aspire.asat.phishing.repository.DomainRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

/**
 * Resolves the tracking base URL by swapping the hostname from {@code tracking.base-url}
 * while preserving the gateway path (e.g. {@code /gateway/phishing}).
 * Custom campaign domains also include {@code tracking.campaign-path-prefix}
 * (e.g. {@code /dev}) to match landing-server nginx routing; platform fallback URLs do not.
 */
@Component
@RequiredArgsConstructor
public class TrackingBaseUrlResolver {

    private final DomainRepository domainRepository;

    @Value("${tracking.base-url}")
    private String fallbackBaseUrl;

    @Value("${tracking.campaign-path-prefix:}")
    private String campaignPathPrefix;

    public String resolve(String clientId, String campaignDomainId, String landingPageDomainId) {
        String domainId = pickDomainId(campaignDomainId, landingPageDomainId);
        if (!StringUtils.hasText(domainId)) {
            return normalizeBaseUrl(fallbackBaseUrl);
        }
        return resolveHostname(clientId, domainId.trim())
                .map(this::swapHost)
                .orElse(normalizeBaseUrl(fallbackBaseUrl));
    }

    public void validateDomainId(String clientId, String domainId) {
        if (!StringUtils.hasText(domainId)) {
            return;
        }
        findUsableDomain(clientId, domainId.trim())
                .orElseThrow(() -> new PhishingValidationException(
                        "Tracking domain not found or not verified: " + domainId.trim()));
    }

    public Optional<String> resolveHostname(String clientId, String domainId) {
        if (!StringUtils.hasText(domainId)) {
            return Optional.empty();
        }
        return findUsableDomain(clientId, domainId.trim())
                .map(Domain::getDomain)
                .map(this::normalizeHost)
                .filter(host -> !host.isEmpty());
    }

    /**
     * Origin for SMS short links: {@code https://{verifiedHost}} with no gateway path prefix.
     * Empty when the campaign has no verified tracking domain.
     */
    public Optional<String> resolveShortLinkOrigin(String clientId, String campaignDomainId, String landingPageDomainId) {
        String domainId = pickDomainId(campaignDomainId, landingPageDomainId);
        if (!StringUtils.hasText(domainId)) {
            return Optional.empty();
        }
        URI fallback = URI.create(normalizeBaseUrl(fallbackBaseUrl));
        String scheme = fallback.getScheme() == null ? "https" : fallback.getScheme();
        return resolveHostname(clientId, domainId.trim())
                .map(host -> scheme + "://" + host);
    }

    private Optional<Domain> findUsableDomain(String clientId, String domainId) {
        return domainRepository.findByIdAndClientIdOrGlobal(domainId, clientId)
                .filter(this::isUsableForTracking);
    }

    private boolean isUsableForTracking(Domain domain) {
        if (domain == null || domain.getStatus() == null) {
            return false;
        }
        return domain.getStatus() == DomainStatus.VERIFIED
                || domain.getStatus() == DomainStatus.VERIFIED_AND_LOCKED;
    }

    private String pickDomainId(String campaignDomainId, String landingPageDomainId) {
        if (StringUtils.hasText(campaignDomainId)) {
            return campaignDomainId.trim();
        }
        if (StringUtils.hasText(landingPageDomainId)) {
            return landingPageDomainId.trim();
        }
        return null;
    }

    private String swapHost(String hostname) {
        URI fallback = URI.create(normalizeBaseUrl(fallbackBaseUrl));
        String path = buildCampaignPath(fallback.getPath());
        return fallback.getScheme() + "://" + normalizeHost(hostname) + path;
    }

    private String buildCampaignPath(String fallbackPath) {
        String prefix = campaignPathPrefix == null ? "" : campaignPathPrefix.trim();
        if (!prefix.isEmpty()) {
            if (!prefix.startsWith("/")) {
                prefix = "/" + prefix;
            }
            if (prefix.endsWith("/")) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
        }
        String path = fallbackPath == null || fallbackPath.isBlank() ? "" : fallbackPath;
        return prefix + path;
    }

    private String normalizeHost(String domain) {
        if (domain == null) {
            return "";
        }
        String value = domain.trim();
        if (value.isEmpty()) {
            return value;
        }
        if (value.contains("://")) {
            value = URI.create(value).getHost();
        }
        int slash = value.indexOf('/');
        if (slash >= 0) {
            value = value.substring(0, slash);
        }
        int colon = value.indexOf(':');
        if (colon >= 0) {
            value = value.substring(0, colon);
        }
        return value.toLowerCase(Locale.ROOT);
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("tracking.base-url is not configured");
        }
        String trimmed = baseUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
