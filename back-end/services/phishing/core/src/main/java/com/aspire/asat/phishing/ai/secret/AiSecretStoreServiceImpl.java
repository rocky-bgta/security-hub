package com.aspire.asat.phishing.ai.secret;

import com.aspire.asat.phishing.ai.model.AiResolvedCredentials;
import com.aspire.asat.phishing.ai.ssm.AwsSsmParameterStoreClient;
import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.dto.request.AiProviderCredentialsRequest;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.model.AiProviderSecretRef;
import com.aspire.asat.phishing.repository.AiProviderSecretRefRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiSecretStoreServiceImpl implements AiSecretStoreService {

    private final AwsSsmParameterStoreClient ssmClient;
    private final AiProviderSecretRefRepository secretRefRepository;
    private final Environment environment;

    @Override
    public AiResolvedCredentials resolveCredentials(
            AiProviderType providerType,
            String clientAdminId,
            AiProviderCredentialsRequest requestCredentials) {

        if (providerType == null) {
            throw new ServiceException("providerType is required");
        }
        if (!StringUtils.hasText(clientAdminId)) {
            throw new ServiceException("clientAdminId is required to resolve credentials");
        }

        String apiKeyFromRequest = requestCredentials != null ? requestCredentials.getApiKey() : null;

        // If user provided credentials, store them in SSM and upsert metadata in DB.
        if (StringUtils.hasText(apiKeyFromRequest)) {
            validateApiKey(providerType, apiKeyFromRequest);

            String apiSecretFromRequest = requestCredentials != null ? requestCredentials.getApiSecret() : null;

            ParameterNames names = buildParameterNames(providerType, clientAdminId);
            putIfMissingOrOverwrite(names.apiKeyParameterName, apiKeyFromRequest, true);

            if (StringUtils.hasText(apiSecretFromRequest)) {
                putIfMissingOrOverwrite(names.apiSecretParameterName, apiSecretFromRequest, true);
            }
            upsertSecretRef(providerType, clientAdminId, requestCredentials, names);

            return AiResolvedCredentials.builder()
                    .apiKey(apiKeyFromRequest)
                    .apiSecret(apiSecretFromRequest)
                    .build();
        }

        // Otherwise: resolve parameter names from DB and fetch SecureString values from SSM.
        Optional<AiProviderSecretRef> secretRefOpt = secretRefRepository
                .findByClientAdminIdAndProviderTypeAndActiveIsTrue(clientAdminId, providerType);

        if (secretRefOpt.isEmpty()) {
            throw new ServiceException("AI provider credentials not configured for this tenant/provider");
        }

        AiProviderSecretRef secretRef = secretRefOpt.get();
        String apiKey = ssmClient.getParameter(secretRef.getApiKeyParameterName());
        if (!StringUtils.hasText(apiKey)) {
            throw new ServiceException("AI provider API key not found in AWS SSM");
        }

        String apiSecret = null;
        if (StringUtils.hasText(secretRef.getApiSecretParameterName())) {
            apiSecret = ssmClient.getParameter(secretRef.getApiSecretParameterName());
        }

        return AiResolvedCredentials.builder()
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .build();
    }

    private void upsertSecretRef(
            AiProviderType providerType,
            String clientAdminId,
            AiProviderCredentialsRequest requestCredentials,
            ParameterNames names) {

        String secretName = requestCredentials != null ? requestCredentials.getSecretName() : null;
        String description = requestCredentials != null ? requestCredentials.getDescription() : null;
        if (!StringUtils.hasText(secretName)) {
            secretName = providerType.name();
        }

        AiProviderSecretRef secretRef = secretRefRepository
                .findByClientAdminIdAndProviderTypeAndActiveIsTrue(clientAdminId, providerType)
                .orElseGet(() -> AiProviderSecretRef.builder()
                        .clientAdminId(clientAdminId)
                        .providerType(providerType)
                        .active(true)
                        .build());

        secretRef.setSecretName(secretName);
        secretRef.setDescription(description);
        secretRef.setApiKeyParameterName(names.apiKeyParameterName);
        secretRef.setApiSecretParameterName(names.apiSecretParameterName);
        secretRef.setActive(true);

        secretRefRepository.save(secretRef);
    }

    private ParameterNames buildParameterNames(AiProviderType providerType, String clientAdminId) {
        String prefix = environment.getProperty("aws.ssm.parameter-path-prefix", "/asat");
        String profile = resolveActiveProfile();
        String serviceName = environment.getProperty("spring.application.name", "phishing-service");

        // Match the same layout style used by SsmParameterInitializer: /{prefix}/{profile}/{serviceName}/...
        String base = normalizeJoin(prefix, profile, serviceName, "ai", clientAdminId, providerType.name());

        return new ParameterNames(
                base + "/apiKey",
                base + "/apiSecret"
        );
    }

    private String resolveActiveProfile() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles != null && profiles.length > 0 && StringUtils.hasText(profiles[0])) {
            return profiles[0];
        }
        return environment.getProperty("spring.profiles.active", "local");
    }

    private String normalizeJoin(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!StringUtils.hasText(part)) {
                continue;
            }
            String cleaned = part;
            if (sb.length() == 0) {
                cleaned = cleaned.startsWith("/") ? cleaned.substring(1) : cleaned;
            }
            cleaned = cleaned.endsWith("/") ? cleaned.substring(0, cleaned.length() - 1) : cleaned;
            if (sb.length() == 0) {
                sb.append("/").append(cleaned);
            } else {
                sb.append("/").append(cleaned);
            }
        }
        return sb.toString();
    }

    private void putIfMissingOrOverwrite(String parameterName, String value, boolean secureString) {
        if (!StringUtils.hasText(parameterName)) {
            throw new ServiceException("SSM parameter name is missing");
        }
        if (value == null) {
            throw new ServiceException("SSM parameter value is missing");
        }

        // Always overwrite: avoids multiple stale versions after rotation.
        ssmClient.putStringParameter(parameterName, value, secureString, true);
    }

    private void validateApiKey(AiProviderType providerType, String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            throw new ServiceException("apiKey is required");
        }

        // Generic sanity checks (avoid accepting empty/obviously wrong values).
        int len = apiKey.trim().length();
        if (len < 20 || len > 5000) {
            throw new ServiceException("apiKey length is invalid");
        }

        // Provider-specific format checks (strict allow-list for OpenAI first).
        if (providerType == AiProviderType.OPENAI) {
            String trimmed = apiKey.trim();
            // OpenAI keys often start with sk-; allow newer prefixes as well.
            if (!trimmed.startsWith("sk-")) {
                log.warn("OPENAI apiKey does not start with expected prefix (not logging key)");
                // Still block to satisfy "strict validation". Adjust later if needed.
                throw new ServiceException("apiKey format is invalid for OpenAI");
            }
        } else if (providerType == AiProviderType.GEMINI) {
            String trimmed = apiKey.trim();
            if (!trimmed.startsWith("AIza")) {
                log.warn("GEMINI apiKey does not start with expected prefix (not logging key)");
                throw new ServiceException("apiKey format is invalid for Gemini");
            }
        } else if (providerType == AiProviderType.ZAI) {
            String trimmed = apiKey.trim();
            if (trimmed.contains(" ") || "YOUR_API_KEY".equalsIgnoreCase(trimmed)) {
                log.warn("ZAI apiKey format looks invalid (not logging key)");
                throw new ServiceException("apiKey format is invalid for ZAI");
            }
        }
    }

    private record ParameterNames(
            String apiKeyParameterName,
            String apiSecretParameterName
    ) {}
}

