package com.aspire.asat.phishing.mapper;

import com.aspire.asat.phishing.dto.request.SmsServerConfigurationRequest;
import com.aspire.asat.phishing.dto.response.SmsServerConfigurationDto;
import com.aspire.asat.phishing.model.SmsServerConfiguration;
import com.aspire.asat.phishing.service.support.CredentialMaskingUtil;
import org.springframework.stereotype.Component;

@Component
public class SmsServerConfigurationMapper {

    public SmsServerConfigurationDto toDto(SmsServerConfiguration entity) {
        if (entity == null) {
            return null;
        }
        return SmsServerConfigurationDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .provider(entity.getProvider())
                .apiKeyMasked(CredentialMaskingUtil.mask(entity.getApiKey()))
                .apiSecretMasked(CredentialMaskingUtil.mask(entity.getApiSecret()))
                .senderId(entity.getSenderId())
                .baseUrl(entity.getBaseUrl())
                .isDefault(entity.isDefault())
                .status(entity.getStatus())
                .providerMetadata(entity.getProviderMetadata())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public SmsServerConfiguration toEntity(SmsServerConfigurationRequest request, String clientId) {
        return SmsServerConfiguration.builder()
                .clientId(clientId)
                .name(request.getName().trim())
                .provider(request.getProvider().trim())
                .senderId(request.getSenderId())
                .baseUrl(request.getBaseUrl())
                .isDefault(request.isDefault())
                .status(request.getStatus() != null ? request.getStatus() : com.aspire.asat.phishing.dto.enums.SmsServerStatus.ACTIVE)
                .providerMetadata(request.getProviderMetadata())
                .build();
    }

    public void applyUpdate(SmsServerConfiguration entity, SmsServerConfigurationRequest request) {
        entity.setName(request.getName().trim());
        entity.setProvider(request.getProvider().trim());
        entity.setSenderId(request.getSenderId());
        entity.setBaseUrl(request.getBaseUrl());
        entity.setDefault(request.isDefault());
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getProviderMetadata() != null) {
            entity.setProviderMetadata(request.getProviderMetadata());
        }
    }
}
