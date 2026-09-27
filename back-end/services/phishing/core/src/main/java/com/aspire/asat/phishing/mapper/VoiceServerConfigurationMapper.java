package com.aspire.asat.phishing.mapper;

import com.aspire.asat.phishing.dto.request.VoiceServerConfigurationRequest;
import com.aspire.asat.phishing.dto.response.VoiceServerConfigurationDto;
import com.aspire.asat.phishing.model.VoiceServerConfiguration;
import com.aspire.asat.phishing.service.support.CredentialMaskingUtil;
import org.springframework.stereotype.Component;

@Component
public class VoiceServerConfigurationMapper {

    public VoiceServerConfigurationDto toDto(VoiceServerConfiguration entity) {
        return toDto(entity, false);
    }

    public VoiceServerConfigurationDto toDto(VoiceServerConfiguration entity, boolean canEdit) {
        if (entity == null) {
            return null;
        }
        return VoiceServerConfigurationDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .provider(entity.getProvider())
                .apiKeyMasked(CredentialMaskingUtil.mask(entity.getApiKey()))
                .apiSecretMasked(CredentialMaskingUtil.mask(entity.getApiSecret()))
                .callerId(entity.getCallerId())
                .baseUrl(entity.getBaseUrl())
                .region(entity.getRegion())
                .countryCode(entity.getCountryCode())
                .isDefault(entity.isDefault())
                .isGlobal(entity.isGlobal())
                .canEdit(canEdit)
                .status(entity.getStatus())
                .providerMetadata(entity.getProviderMetadata())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public VoiceServerConfiguration toEntity(VoiceServerConfigurationRequest request, String clientId) {
        return VoiceServerConfiguration.builder()
                .clientId(clientId)
                .name(request.getName().trim())
                .provider(request.getProvider())
                .callerId(request.getCallerId())
                .baseUrl(request.getBaseUrl())
                .region(request.getRegion())
                .countryCode(request.getCountryCode())
                .isDefault(request.isDefault())
                .status(request.getStatus() != null ? request.getStatus()
                        : com.aspire.asat.phishing.dto.enums.VoiceServerStatus.ACTIVE)
                .providerMetadata(request.getProviderMetadata())
                .build();
    }

    public void applyUpdate(VoiceServerConfiguration entity, VoiceServerConfigurationRequest request) {
        entity.setName(request.getName().trim());
        entity.setProvider(request.getProvider());
        entity.setCallerId(request.getCallerId());
        entity.setBaseUrl(request.getBaseUrl());
        entity.setRegion(request.getRegion());
        entity.setCountryCode(request.getCountryCode());
        entity.setDefault(request.isDefault());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getProviderMetadata() != null) {
            entity.setProviderMetadata(request.getProviderMetadata());
        }
    }
}
