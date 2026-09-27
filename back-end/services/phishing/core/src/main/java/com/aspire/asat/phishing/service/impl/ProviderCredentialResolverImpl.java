package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import com.aspire.asat.phishing.model.ProviderCredential;
import com.aspire.asat.phishing.repository.ProviderCredentialRepository;
import com.aspire.asat.phishing.service.ProviderCredentialResolver;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import com.aspire.asat.phishing.util.ProviderNameNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Default {@link ProviderCredentialResolver}. Prefers an active per-client
 * credential; otherwise falls back to the platform YAML defaults so existing
 * deployments keep working until clients configure their own keys.
 */
@Service
@Slf4j
public class ProviderCredentialResolverImpl implements ProviderCredentialResolver {

    private static final String ELEVENLABS = "ELEVENLABS";
    private static final String FISH_AUDIO = "FISH_AUDIO";
    private static final String HEYGEN = "HEYGEN";

    private final ProviderCredentialRepository providerCredentialRepository;
    private final CredentialEncryptionService credentialEncryptionService;

    private final String elevenLabsKey;
    private final String elevenLabsBaseUrl;
    private final String fishKey;
    private final String fishBaseUrl;
    private final String heyGenKey;
    private final String heyGenBaseUrl;

    public ProviderCredentialResolverImpl(
            ProviderCredentialRepository providerCredentialRepository,
            CredentialEncryptionService credentialEncryptionService,
            @Value("${elevenlabs.api.key:}") String elevenLabsKey,
            @Value("${elevenlabs.base-url:}") String elevenLabsBaseUrl,
            @Value("${fish.api.key:}") String fishKey,
            @Value("${fish.base-url:}") String fishBaseUrl,
            @Value("${heygen.api.key:}") String heyGenKey,
            @Value("${heygen.base-url:}") String heyGenBaseUrl) {
        this.providerCredentialRepository = providerCredentialRepository;
        this.credentialEncryptionService = credentialEncryptionService;
        this.elevenLabsKey = elevenLabsKey;
        this.elevenLabsBaseUrl = elevenLabsBaseUrl;
        this.fishKey = fishKey;
        this.fishBaseUrl = fishBaseUrl;
        this.heyGenKey = heyGenKey;
        this.heyGenBaseUrl = heyGenBaseUrl;
    }

    @Override
    public ResolvedProviderCredentials resolve(String clientId, String providerName) {
        String provider = providerName == null ? "" : providerName.trim();
        String canonical = ProviderNameNormalizer.canonicalize(provider);
        String lookupName = canonical != null ? canonical : provider;

        if (StringUtils.hasText(clientId) && StringUtils.hasText(lookupName)) {
            List<ProviderCredential> creds = findCredentials(clientId, lookupName, provider);
            Optional<ProviderCredential> activeCred = creds.stream()
                    .filter(cred -> !Boolean.FALSE.equals(cred.getIsActive()))
                    .findFirst();
            if (activeCred.isPresent()) {
                return toResolved(activeCred.get(), lookupName);
            }
            if (!creds.isEmpty()) {
                log.debug("Provider credential for client={} provider={} is inactive; using fallback",
                        clientId, lookupName);
            }
        }

        return fallback(lookupName);
    }

    private List<ProviderCredential> findCredentials(String clientId, String canonical, String original) {
        List<ProviderCredential> creds = providerCredentialRepository
                .findByClientIdAndProviderNameIgnoreCase(clientId, canonical);
        if (!creds.isEmpty()) {
            return creds;
        }
        if (StringUtils.hasText(original) && !original.equalsIgnoreCase(canonical)) {
            return providerCredentialRepository.findByClientIdAndProviderNameIgnoreCase(clientId, original);
        }
        String spaced = canonical.replace('_', ' ');
        if (!spaced.equalsIgnoreCase(canonical)) {
            return providerCredentialRepository.findByClientIdAndProviderNameIgnoreCase(clientId, spaced);
        }
        return creds;
    }

    @Override
    public ResolvedProviderCredentials resolveById(String clientId, String providerId) {
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(providerId)) {
            throw new ServiceException("clientId and providerId are required", HttpStatus.BAD_REQUEST);
        }
        ProviderCredential cred = providerCredentialRepository
                .findByIdAndClientId(providerId.trim(), clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Provider credential not found: " + providerId.trim()));
        if (Boolean.FALSE.equals(cred.getIsActive())) {
            throw new ServiceException(
                    "Provider credential is inactive: " + providerId.trim(), HttpStatus.BAD_REQUEST);
        }
        return toResolved(cred, cred.getProviderName());
    }

    private ResolvedProviderCredentials toResolved(ProviderCredential cred, String providerName) {
        return ResolvedProviderCredentials.builder()
                .providerName(StringUtils.hasText(providerName) ? providerName : cred.getProviderName())
                .apiKey(decrypt(cred.getApiKey()))
                .apiSecret(decrypt(cred.getApiSecret()))
                .baseUrl(cred.getBaseUrl())
                .modelName(cred.getModelName())
                .category(cred.getCategory())
                .build();
    }

    private ResolvedProviderCredentials fallback(String providerName) {
        String normalized = ProviderNameNormalizer.canonicalize(providerName);
        if (normalized == null) {
            normalized = providerName == null ? "" : providerName.toUpperCase(Locale.ROOT);
        }
        return switch (normalized) {
            case ELEVENLABS -> ResolvedProviderCredentials.builder()
                    .providerName(ELEVENLABS).apiKey(elevenLabsKey).baseUrl(nullIfBlank(elevenLabsBaseUrl)).build();
            case FISH_AUDIO -> ResolvedProviderCredentials.builder()
                    .providerName(FISH_AUDIO).apiKey(fishKey).baseUrl(nullIfBlank(fishBaseUrl)).build();
            case HEYGEN -> ResolvedProviderCredentials.builder()
                    .providerName(HEYGEN).apiKey(heyGenKey).baseUrl(nullIfBlank(heyGenBaseUrl)).build();
            default -> ResolvedProviderCredentials.builder().providerName(providerName).build();
        };
    }

    private String decrypt(String stored) {
        return StringUtils.hasText(stored) ? credentialEncryptionService.decrypt(stored) : null;
    }

    private String nullIfBlank(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
