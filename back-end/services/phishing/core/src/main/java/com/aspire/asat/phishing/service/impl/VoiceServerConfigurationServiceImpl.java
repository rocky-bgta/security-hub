package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.common.util.PhoneNumberUtils;
import com.aspire.asat.phishing.dto.enums.ConfigurationAuditEntityType;
import com.aspire.asat.phishing.dto.enums.VoiceProviderType;
import com.aspire.asat.phishing.dto.enums.VoiceServerStatus;
import com.aspire.asat.phishing.dto.request.VoiceServerConfigurationRequest;
import com.aspire.asat.phishing.dto.request.VoiceServerTestRequest;
import com.aspire.asat.phishing.dto.response.TestResultDto;
import com.aspire.asat.phishing.dto.response.VoiceServerConfigurationDto;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.mapper.VoiceServerConfigurationMapper;
import com.aspire.asat.phishing.model.VoiceServerConfiguration;
import com.aspire.asat.phishing.repository.VoiceServerConfigurationRepository;
import com.aspire.asat.phishing.service.ConfigurationAuditService;
import com.aspire.asat.phishing.service.VoiceServerConfigurationService;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import com.aspire.asat.phishing.voice.VoiceCallRequest;
import com.aspire.asat.phishing.voice.VoiceCallResult;
import com.aspire.asat.phishing.voice.VoiceProvider;
import com.aspire.asat.phishing.voice.VoiceProviderFactory;
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
public class VoiceServerConfigurationServiceImpl implements VoiceServerConfigurationService {

    private final VoiceServerConfigurationRepository repository;
    private final VoiceServerConfigurationMapper mapper;
    private final CredentialEncryptionService credentialEncryptionService;
    private final ConfigurationAuditService configurationAuditService;
    private final UserCurrentContextService userCurrentContextService;
    private final VoiceProviderFactory voiceProviderFactory;

    @Override
    public List<VoiceServerConfigurationDto> getConfigurations(int offset, int pageSize, String clientId) {
        UserType userType = getCurrentUserType();
        Pageable pageable = pageable(offset, pageSize);

        if (isPlatformAdmin(userType)) {
            if (hasText(clientId)) {
                return repository.findByClientId(clientId.trim(), pageable).stream()
                        .map(c -> mapper.toDto(c, true))
                        .collect(Collectors.toList());
            }
            return repository.findAll(pageable).stream()
                    .map(c -> mapper.toDto(c, true))
                    .collect(Collectors.toList());
        }

        String ownClientId = resolveOwnClientId();
        return repository.findByClientIdOrGlobal(ownClientId, pageable).stream()
                .map(c -> mapper.toDto(c, canEdit(c, userType, ownClientId)))
                .collect(Collectors.toList());
    }

    @Override
    public long countConfigurations(String clientId) {
        if (isPlatformAdmin(getCurrentUserType())) {
            if (hasText(clientId)) {
                return repository.countByClientId(clientId.trim());
            }
            return repository.count();
        }
        return repository.countByClientIdOrGlobal(resolveOwnClientId());
    }

    @Override
    public VoiceServerConfigurationDto getById(String id) {
        UserType userType = getCurrentUserType();
        String ownClientId = resolveOwnClientId();
        VoiceServerConfiguration config = findAccessible(id, userType, ownClientId);
        return mapper.toDto(config, canEdit(config, userType, ownClientId));
    }

    @Override
    @Transactional
    public VoiceServerConfigurationDto create(VoiceServerConfigurationRequest request) {
        UserType userType = getCurrentUserType();
        String clientId = resolveOwnClientId();
        boolean global = isPlatformAdmin(userType);
        validateRequest(request, true);
        assertNameUnique(request.getName().trim(), null, global, clientId);

        VoiceServerConfiguration entity = mapper.toEntity(request, clientId);
        entity.setGlobal(global);
        entity.setApiKey(credentialEncryptionService.encrypt(requireText(request.getApiKey(), "API key")));
        entity.setApiSecret(credentialEncryptionService.encrypt(requireText(request.getApiSecret(), "API secret")));

        if (entity.isDefault()) {
            clearDefaultsForOwnership(entity);
        }

        VoiceServerConfiguration saved = repository.save(entity);
        configurationAuditService.logChange(clientId, ConfigurationAuditEntityType.VOICE_SERVER, saved.getId(),
                "CREATE", null, auditSnapshot(saved));
        return mapper.toDto(saved, true);
    }

    @Override
    @Transactional
    public VoiceServerConfigurationDto update(String id, VoiceServerConfigurationRequest request) {
        UserType userType = getCurrentUserType();
        String ownClientId = resolveOwnClientId();
        validateRequest(request, false);
        VoiceServerConfiguration existing = findAccessible(id, userType, ownClientId);
        assertCanEdit(existing, userType, ownClientId);

        assertNameUnique(request.getName().trim(), id, existing.isGlobal(), existing.getClientId());

        Map<String, Object> before = auditSnapshot(existing);
        mapper.applyUpdate(existing, request);

        if (hasText(request.getApiKey())) {
            existing.setApiKey(credentialEncryptionService.encrypt(request.getApiKey().trim()));
        }
        if (hasText(request.getApiSecret())) {
            existing.setApiSecret(credentialEncryptionService.encrypt(request.getApiSecret().trim()));
        }

        if (existing.isDefault()) {
            clearDefaultsForOwnership(existing);
        }

        VoiceServerConfiguration saved = repository.save(existing);
        configurationAuditService.logChange(ownClientId, ConfigurationAuditEntityType.VOICE_SERVER, saved.getId(),
                "UPDATE", before, auditSnapshot(saved));
        return mapper.toDto(saved, true);
    }

    @Override
    @Transactional
    public void delete(String id) {
        UserType userType = getCurrentUserType();
        String ownClientId = resolveOwnClientId();
        VoiceServerConfiguration existing = findAccessible(id, userType, ownClientId);
        assertCanEdit(existing, userType, ownClientId);
        Map<String, Object> before = auditSnapshot(existing);
        repository.delete(existing);
        configurationAuditService.logChange(existing.getClientId(), ConfigurationAuditEntityType.VOICE_SERVER, id,
                "DELETE", before, null);
    }

    @Override
    public VoiceServerConfigurationDto getDefault() {
        String clientId = resolveOwnClientId();
        UserType userType = getCurrentUserType();
        VoiceServerConfiguration config = repository
                .findByClientIdAndIsDefaultTrueAndStatus(clientId, VoiceServerStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Default voice server not found"));
        return mapper.toDto(config, canEdit(config, userType, clientId));
    }

    @Override
    @Transactional
    public VoiceServerConfigurationDto setDefault(String id) {
        UserType userType = getCurrentUserType();
        String ownClientId = resolveOwnClientId();
        VoiceServerConfiguration config = findAccessible(id, userType, ownClientId);
        assertCanEdit(config, userType, ownClientId);
        if (config.getStatus() != VoiceServerStatus.ACTIVE) {
            throw new PhishingValidationException("Only ACTIVE voice servers can be set as default");
        }
        clearDefaultsForOwnership(config);
        config.setDefault(true);
        VoiceServerConfiguration saved = repository.save(config);
        configurationAuditService.logChange(ownClientId, ConfigurationAuditEntityType.VOICE_SERVER, id,
                "SET_DEFAULT", null, auditSnapshot(saved));
        return mapper.toDto(saved, true);
    }

    @Override
    public TestResultDto testConfiguration(String id, VoiceServerTestRequest request) {
        UserType userType = getCurrentUserType();
        String ownClientId = resolveOwnClientId();
        VoiceServerConfiguration config = findAccessible(id, userType, ownClientId);
        assertCanEdit(config, userType, ownClientId);
        String phone = PhoneNumberUtils.normalize(request.getPhoneNumber());
        if (!PhoneNumberUtils.isValidE164(phone)) {
            throw new PhishingValidationException("Invalid phone number; use E.164 format");
        }
        String script = hasText(request.getScriptBody())
                ? request.getScriptBody()
                : "This is an ASAT voice server configuration test call.";
        VoiceProvider provider = voiceProviderFactory.create(config);
        VoiceCallResult result = provider.initiateCall(VoiceCallRequest.builder()
                .toPhone(phone)
                .scriptBody(script)
                .callerId(config.getCallerId())
                .build());
        if (result.success()) {
            return TestResultDto.builder().success(true).message("Test call initiated successfully").build();
        }
        return TestResultDto.builder()
                .success(false)
                .message(result.errorMessage() != null ? result.errorMessage() : "Voice call failed")
                .build();
    }

    @Override
    public VoiceServerConfiguration resolveForCampaign(String clientId, String configurationId) {
        if (hasText(configurationId)) {
            VoiceServerConfiguration config = repository.findByIdAndClientIdOrGlobal(configurationId, clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Voice server configuration not found"));
            if (config.getStatus() != VoiceServerStatus.ACTIVE) {
                throw new PhishingValidationException("Voice server configuration is not active");
            }
            return config;
        }
        return repository.findByClientIdAndIsDefaultTrueAndStatus(clientId, VoiceServerStatus.ACTIVE)
                .orElseThrow(() -> new PhishingValidationException("No default voice server configured"));
    }

    private VoiceServerConfiguration findAccessible(String id, UserType userType, String ownClientId) {
        if (isPlatformAdmin(userType)) {
            return repository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Voice server configuration not found"));
        }
        return repository.findByIdAndClientIdOrGlobal(id, ownClientId)
                .orElseThrow(() -> new ResourceNotFoundException("Voice server configuration not found"));
    }

    private void assertCanEdit(VoiceServerConfiguration config, UserType userType, String ownClientId) {
        if (!canEdit(config, userType, ownClientId)) {
            throw new PhishingValidationException(
                    "You do not have permission to modify this voice server configuration");
        }
    }

    private boolean canEdit(VoiceServerConfiguration config, UserType userType, String ownClientId) {
        if (isPlatformAdmin(userType)) {
            return true;
        }
        return config != null
                && !config.isGlobal()
                && ownClientId != null
                && ownClientId.equals(config.getClientId());
    }

    private void assertNameUnique(String name, String excludeId, boolean global, String clientId) {
        boolean duplicate;
        if (global) {
            duplicate = excludeId == null
                    ? repository.existsByIsGlobalTrueAndName(name)
                    : repository.existsByIsGlobalTrueAndNameAndIdNot(name, excludeId);
        } else {
            duplicate = excludeId == null
                    ? repository.existsByClientIdAndName(clientId, name)
                    : repository.existsByClientIdAndNameAndIdNot(clientId, name, excludeId);
        }
        if (duplicate) {
            throw new PhishingValidationException("Voice server name already exists");
        }
    }

    private void clearDefaultsForOwnership(VoiceServerConfiguration target) {
        if (target.isGlobal()) {
            repository.findByIsGlobalTrueAndIsDefaultTrue().forEach(config -> {
                if (!config.getId().equals(target.getId())) {
                    config.setDefault(false);
                    repository.save(config);
                }
            });
            return;
        }
        clearDefaultForClientExcept(target.getClientId(), target.getId());
    }

    private void clearDefaultForClientExcept(String clientId, String exceptId) {
        repository.findByClientId(clientId, Pageable.unpaged()).forEach(config -> {
            if (!config.getId().equals(exceptId) && config.isDefault()) {
                config.setDefault(false);
                repository.save(config);
            }
        });
    }

    private String resolveOwnClientId() {
        return userCurrentContextService.getCurrentUserContext().getClientAdminId();
    }

    private UserType getCurrentUserType() {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        try {
            return UserType.fromString(context.getUserType());
        } catch (Exception e) {
            log.warn("Unable to parse userType from context: {}", context.getUserType());
            return null;
        }
    }

    private boolean isPlatformAdmin(UserType userType) {
        return userType == UserType.SUPER_ADMIN
                || userType == UserType.ASPIRE_ADMIN
                || userType == UserType.SYSTEM_USER;
    }

    private Pageable pageable(int offset, int pageSize) {
        return PageRequest.of(Math.max(0, offset), Math.max(1, pageSize),
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private void validateRequest(VoiceServerConfigurationRequest request, boolean isCreate) {
        if (request.getProvider() == VoiceProviderType.GENERIC_SIP && !hasText(request.getBaseUrl())) {
            throw new PhishingValidationException("Base URL is required for GENERIC_SIP provider");
        }
        if (isCreate) {
            if (!hasText(request.getApiKey()) || !hasText(request.getApiSecret())) {
                throw new PhishingValidationException("API key and API secret are required");
            }
        }
    }

    private Map<String, Object> auditSnapshot(VoiceServerConfiguration config) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("name", config.getName());
        snapshot.put("provider", config.getProvider());
        snapshot.put("callerId", config.getCallerId());
        snapshot.put("baseUrl", config.getBaseUrl());
        snapshot.put("isDefault", config.isDefault());
        snapshot.put("isGlobal", config.isGlobal());
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
