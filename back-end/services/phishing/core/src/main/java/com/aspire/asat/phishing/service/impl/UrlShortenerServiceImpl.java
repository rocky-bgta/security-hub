package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.ShortUrl;
import com.aspire.asat.phishing.repository.ShortUrlRepository;
import com.aspire.asat.phishing.service.UrlShortenerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlShortenerServiceImpl implements UrlShortenerService {

    /**
     * Unambiguous alphabet (no 0/O/1/I/l) for the random suffix (e.g. {@code k7Qm2Nxp}).
     * Env prefixes intentionally use {@code 0}/{@code 1} so they cannot appear as a random-only code.
     */
    static final String ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    /** Development / Staging / Production short-code prefixes. */
    static final Set<String> ALLOWED_ENV_PREFIXES = Set.of("01", "10", "11");

    private static final int MAX_GENERATE_ATTEMPTS = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ShortUrlRepository shortUrlRepository;

    /** Length of the random suffix (not including the env prefix). */
    @Value("${shortener.code-length:8}")
    private int codeLength;

    /**
     * Two-digit environment marker prepended to every short code:
     * {@code 01} = development, {@code 10} = staging, {@code 11} = production.
     */
    @Value("${shortener.env-prefix:}")
    private String envPrefix;

    @Override
    public String shorten(String originalUrl, String shortOrigin, CampaignRecipient recipient) {
        if (recipient == null || !StringUtils.hasText(recipient.getTrackingId())) {
            throw new IllegalArgumentException("Recipient trackingId is required to shorten a URL");
        }
        if (!StringUtils.hasText(originalUrl)) {
            throw new IllegalArgumentException("Original URL is required to shorten a URL");
        }
        String origin = normalizeOrigin(shortOrigin);
        String prefix = requireEnvPrefix();

        Optional<ShortUrl> existing = shortUrlRepository.findByTrackingId(recipient.getTrackingId());
        if (existing.isPresent()) {
            return existing.get().getShortUrl();
        }

        int randomLength = effectiveRandomLength();
        for (int attempt = 1; attempt <= MAX_GENERATE_ATTEMPTS; attempt++) {
            String shortCode = generateCode(prefix, randomLength);
            String shortUrl = origin + "/" + shortCode;
            ShortUrl entity = ShortUrl.builder()
                    .shortCode(shortCode)
                    .trackingId(recipient.getTrackingId())
                    .originalUrl(originalUrl)
                    .shortUrl(shortUrl)
                    .campaignId(recipient.getCampaignId())
                    .recipientId(recipient.getId())
                    .clientId(recipient.getClientId())
                    .createdAt(Instant.now())
                    .build();
            try {
                shortUrlRepository.save(entity);
                return shortUrl;
            } catch (DuplicateKeyException e) {
                Optional<ShortUrl> raced = shortUrlRepository.findByTrackingId(recipient.getTrackingId());
                if (raced.isPresent()) {
                    return raced.get().getShortUrl();
                }
                log.debug("Short code collision on attempt {} for trackingId={}", attempt, recipient.getTrackingId());
            }
        }
        throw new IllegalStateException("Failed to generate a unique short code for trackingId="
                + recipient.getTrackingId());
    }

    @Override
    public Optional<String> resolve(String shortCode) {
        if (!StringUtils.hasText(shortCode)) {
            return Optional.empty();
        }
        return shortUrlRepository.findByShortCode(shortCode.trim())
                .map(ShortUrl::getOriginalUrl)
                .filter(StringUtils::hasText);
    }

    @Override
    public String previewUrl(String shortOrigin) {
        String prefix = requireEnvPrefix();
        return normalizeOrigin(shortOrigin) + "/" + prefix + sampleSuffix(effectiveRandomLength());
    }

    private String generateCode(String prefix, int randomLength) {
        return prefix + randomSuffix(randomLength);
    }

    private static String randomSuffix(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }

    private static String sampleSuffix(int length) {
        return "X".repeat(Math.max(1, length));
    }

    private int effectiveRandomLength() {
        return codeLength > 0 ? codeLength : 8;
    }

    private String requireEnvPrefix() {
        if (!StringUtils.hasText(envPrefix)) {
            throw new IllegalStateException(
                    "shortener.env-prefix is required (01=dev, 10=staging, 11=production)");
        }
        String prefix = envPrefix.trim();
        if (!ALLOWED_ENV_PREFIXES.contains(prefix)) {
            throw new IllegalStateException(
                    "shortener.env-prefix must be one of " + ALLOWED_ENV_PREFIXES + ", got: " + prefix);
        }
        return prefix;
    }

    private static String normalizeOrigin(String shortOrigin) {
        if (!StringUtils.hasText(shortOrigin)) {
            throw new IllegalArgumentException("Short link origin is required");
        }
        String trimmed = shortOrigin.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
