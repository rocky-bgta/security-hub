package com.aspire.asat.phishing.mapper;

import com.aspire.asat.phishing.dto.request.ProviderCredentialCreateRequest;
import com.aspire.asat.phishing.dto.request.ProviderCredentialUpdateRequest;
import com.aspire.asat.phishing.dto.response.ProviderCredentialDto;
import com.aspire.asat.phishing.model.ProviderCredential;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Maps between {@link ProviderCredential} entities and DTOs.
 *
 * <p>Secrets are set here as plaintext on create/update; the service layer is
 * responsible for encrypting them before persistence. On the read path, only a
 * masked representation of the key is exposed.
 */
@Component
public class ProviderCredentialMapper {

    /**
     * Build a new entity from a create request. The raw secrets are attached
     * as-is and must be encrypted by the service before saving.
     */
    public ProviderCredential toEntity(ProviderCredentialCreateRequest request, String clientId) {
        if (request == null) {
            return null;
        }
        return ProviderCredential.builder()
                .clientId(clientId)
                .providerName(normalize(request.getProviderName()))
                .category(request.getCategory())
                .modelName(trimToNull(request.getModelName()))
                .apiKey(request.getApiKey())
                .apiSecret(trimToNull(request.getApiSecret()))
                .baseUrl(trimToNull(request.getBaseUrl()))
                .isActive(request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive()))
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .build();
    }

    /**
     * Apply metadata from an update request. Secrets are only overwritten when a
     * non-blank value is supplied; the service encrypts any updated secret.
     */
    public void updateFromRequest(ProviderCredential entity, ProviderCredentialUpdateRequest request) {
        if (entity == null || request == null) {
            return;
        }
        entity.setProviderName(normalize(request.getProviderName()));
        entity.setCategory(request.getCategory());
        entity.setModelName(trimToNull(request.getModelName()));
        entity.setBaseUrl(trimToNull(request.getBaseUrl()));
        entity.setIsActive(request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive()));
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));

        if (StringUtils.hasText(request.getApiKey())) {
            entity.setApiKey(request.getApiKey());
        }
        if (StringUtils.hasText(request.getApiSecret())) {
            entity.setApiSecret(request.getApiSecret());
        }
    }

    /**
     * Build a masked response DTO. {@code apiKeyLast4} is derived from the
     * already-decrypted key provided by the service.
     */
    public ProviderCredentialDto toDto(ProviderCredential entity, String decryptedApiKey) {
        if (entity == null) {
            return null;
        }
        return ProviderCredentialDto.builder()
                .id(entity.getId())
                .providerName(entity.getProviderName())
                .category(entity.getCategory())
                .modelName(entity.getModelName())
                .apiKeyLast4(last4(decryptedApiKey))
                .hasApiSecret(StringUtils.hasText(entity.getApiSecret()))
                .baseUrl(entity.getBaseUrl())
                .isActive(entity.getIsActive() == null || Boolean.TRUE.equals(entity.getIsActive()))
                .isDefault(Boolean.TRUE.equals(entity.getIsDefault()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String last4(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= 4 ? trimmed : trimmed.substring(trimmed.length() - 4);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
