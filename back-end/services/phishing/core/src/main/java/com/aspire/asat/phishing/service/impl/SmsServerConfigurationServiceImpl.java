package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.ConfigurationAuditEntityType;
import com.aspire.asat.phishing.dto.enums.SmsServerStatus;
import com.aspire.asat.phishing.dto.request.SmsServerConfigurationRequest;
import com.aspire.asat.phishing.dto.request.SmsServerTestRequest;
import com.aspire.asat.phishing.dto.response.SmsServerConfigurationDto;
import com.aspire.asat.phishing.dto.response.TestResultDto;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.mapper.SmsServerConfigurationMapper;
import com.aspire.asat.phishing.model.SmsServerConfiguration;
import com.aspire.asat.phishing.repository.SmsServerConfigurationRepository;
import com.aspire.asat.phishing.service.ConfigurationAuditService;
import com.aspire.asat.phishing.service.SmsServerConfigurationService;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import com.aspire.asat.phishing.sms.SmsProvider;
import com.aspire.asat.phishing.sms.SmsProviderFactory;
import com.aspire.asat.phishing.sms.SmsSendResult;
import com.aspire.asat.common.util.PhoneNumberUtils;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServerConfigurationServiceImpl implements SmsServerConfigurationService {

    private final SmsServerConfigurationRepository repository;
    private final SmsServerConfigurationMapper mapper;
    private final CredentialEncryptionService credentialEncryptionService;
    private final ConfigurationAuditService configurationAuditService;
    private final UserCurrentContextService userCurrentContextService;
    private final SmsProviderFactory smsProviderFactory;

    @Override
    public List<SmsServerConfigurationDto> getConfigurations(int offset, int pageSize, String clientId) {
        String effectiveClientId = resolveClientId(clientId);
        Pageable pageable = PageRequest.of(Math.max(0, offset), Math.max(1, pageSize), Sort.by(Sort.Direction.DESC, "createdAt"));
        return repository.findByClientId(effectiveClientId, pageable).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countConfigurations(String clientId) {
        return repository.countByClientId(resolveClientId(clientId));
    }

    @Override
    public SmsServerConfigurationDto getById(String id) {
        SmsServerConfiguration config = findAccessible(id);
        return mapper.toDto(config);
    }

    @Override
    @Transactional
    public SmsServerConfigurationDto create(SmsServerConfigurationRequest request) {
        String clientId = resolveClientId(null);
        validateRequest(request, true);
        if (repository.existsByClientIdAndName(clientId, request.getName().trim())) {
            throw new PhishingValidationException("SMS server name already exists");
        }

        SmsServerConfiguration entity = mapper.toEntity(request, clientId);
        entity.setApiKey(credentialEncryptionService.encrypt(requireText(request.getApiKey(), "API key")));
        entity.setApiSecret(credentialEncryptionService.encrypt(requireText(request.getApiSecret(), "API secret")));

        if (entity.isDefault()) {
            clearDefaultForClient(clientId);
        }

        SmsServerConfiguration saved = repository.save(entity);
        configurationAuditService.logChange(clientId, ConfigurationAuditEntityType.SMS_SERVER, saved.getId(),
                "CREATE", null, auditSnapshot(saved));
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public SmsServerConfigurationDto update(String id, SmsServerConfigurationRequest request) {
        String clientId = resolveClientId(null);
        validateRequest(request, false);
        SmsServerConfiguration existing = findAccessible(id);

        if (repository.existsByClientIdAndNameAndIdNot(clientId, request.getName().trim(), id)) {
            throw new PhishingValidationException("SMS server name already exists");
        }

        Map<String, Object> before = auditSnapshot(existing);
        mapper.applyUpdate(existing, request);

        if (hasText(request.getApiKey())) {
            existing.setApiKey(credentialEncryptionService.encrypt(request.getApiKey().trim()));
        }
        if (hasText(request.getApiSecret())) {
            existing.setApiSecret(credentialEncryptionService.encrypt(request.getApiSecret().trim()));
        }

        if (existing.isDefault()) {
            clearDefaultForClientExcept(clientId, id);
        }

        SmsServerConfiguration saved = repository.save(existing);
        configurationAuditService.logChange(clientId, ConfigurationAuditEntityType.SMS_SERVER, saved.getId(),
                "UPDATE", before, auditSnapshot(saved));
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(String id) {
        SmsServerConfiguration existing = findAccessible(id);
        Map<String, Object> before = auditSnapshot(existing);
        repository.delete(existing);
        configurationAuditService.logChange(existing.getClientId(), ConfigurationAuditEntityType.SMS_SERVER, id,
                "DELETE", before, null);
    }

    @Override
    public SmsServerConfigurationDto getDefault() {
        String clientId = resolveClientId(null);
        return repository.findByClientIdAndIsDefaultTrueAndStatus(clientId, SmsServerStatus.ACTIVE)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Default SMS server not found"));
    }

    @Override
    @Transactional
    public SmsServerConfigurationDto setDefault(String id) {
        String clientId = resolveClientId(null);
        SmsServerConfiguration config = findAccessible(id);
        if (config.getStatus() != SmsServerStatus.ACTIVE) {
            throw new PhishingValidationException("Only ACTIVE SMS servers can be set as default");
        }
        clearDefaultForClient(clientId);
        config.setDefault(true);
        SmsServerConfiguration saved = repository.save(config);
        configurationAuditService.logChange(clientId, ConfigurationAuditEntityType.SMS_SERVER, id,
                "SET_DEFAULT", null, auditSnapshot(saved));
        return mapper.toDto(saved);
    }

    @Override
    public TestResultDto testConfiguration(String id, SmsServerTestRequest request) {
        SmsServerConfiguration config = findAccessible(id);
        String phone = PhoneNumberUtils.normalize(request.getPhoneNumber());
        if (!PhoneNumberUtils.isValidE164(phone)) {
            throw new PhishingValidationException("Invalid phone number; use E.164 format");
        }
        String message = hasText(request.getMessage())
                ? request.getMessage()
                : "ASAT SMS server configuration test";
        SmsProvider provider = smsProviderFactory.create(config);
        SmsSendResult result = provider.send(phone, message);
        if (result.success()) {
            return TestResultDto.builder().success(true).message("Test SMS sent successfully").build();
        }
        return TestResultDto.builder()
                .success(false)
                .message(result.errorMessage() != null ? result.errorMessage() : "SMS send failed")
                .build();
    }

    @Override
    public SmsServerConfiguration resolveForCampaign(String clientId, String configurationId) {
        if (hasText(configurationId)) {
            SmsServerConfiguration config = repository.findByIdAndClientId(configurationId, clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("SMS server configuration not found"));
            if (config.getStatus() != SmsServerStatus.ACTIVE) {
                throw new PhishingValidationException("SMS server configuration is not active");
            }
            return config;
        }
        return repository.findByClientIdAndIsDefaultTrueAndStatus(clientId, SmsServerStatus.ACTIVE)
                .orElseThrow(() -> new PhishingValidationException("No default SMS server configured"));
    }

    private SmsServerConfiguration findAccessible(String id) {
        String clientId = resolveClientId(null);
        return repository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("SMS server configuration not found"));
    }

    private String resolveClientId(String clientId) {
        if (hasText(clientId)) {
            return clientId.trim();
        }
        return userCurrentContextService.getCurrentUserContext().getClientAdminId();
    }

    private void validateRequest(SmsServerConfigurationRequest request, boolean isCreate) {
        if (!SmsProviderFactory.isTwilio(request.getProvider()) && !hasText(request.getBaseUrl())) {
            throw new PhishingValidationException("Base URL is required for non-Twilio providers");
        }
        if (isCreate) {
            if (!hasText(request.getApiKey()) || !hasText(request.getApiSecret())) {
                throw new PhishingValidationException("API key and API secret are required");
            }
        }
    }

    private void clearDefaultForClient(String clientId) {
        repository.findByClientIdAndIsDefaultTrueAndStatus(clientId, SmsServerStatus.ACTIVE)
                .ifPresent(config -> {
                    config.setDefault(false);
                    repository.save(config);
                });
        repository.findByClientIdAndStatus(clientId, SmsServerStatus.INACTIVE).stream()
                .filter(SmsServerConfiguration::isDefault)
                .forEach(config -> {
                    config.setDefault(false);
                    repository.save(config);
                });
    }

    private void clearDefaultForClientExcept(String clientId, String exceptId) {
        repository.findByClientId(clientId, Pageable.unpaged()).forEach(config -> {
            if (!config.getId().equals(exceptId) && config.isDefault()) {
                config.setDefault(false);
                repository.save(config);
            }
        });
    }

    private Map<String, Object> auditSnapshot(SmsServerConfiguration config) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("name", config.getName());
        snapshot.put("provider", config.getProvider());
        snapshot.put("senderId", config.getSenderId());
        snapshot.put("baseUrl", config.getBaseUrl());
        snapshot.put("isDefault", config.isDefault());
        snapshot.put("status", config.getStatus());
        return snapshot;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String requireText(String value, String field) {
        if (!hasText(value)) {
            throw new PhishingValidationException(field + " is required");
        }
        return value.trim();
    }
}
