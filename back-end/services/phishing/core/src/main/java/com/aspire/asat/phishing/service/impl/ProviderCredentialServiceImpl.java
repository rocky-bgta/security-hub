package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.ProviderCategory;
import com.aspire.asat.phishing.dto.request.ProviderCredentialCreateRequest;
import com.aspire.asat.phishing.dto.request.ProviderCredentialUpdateRequest;
import com.aspire.asat.phishing.dto.response.ProviderCredentialDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.mapper.ProviderCredentialMapper;
import com.aspire.asat.phishing.media.adapter.VoiceCloneAdapterFactory;
import com.aspire.asat.phishing.model.ProviderCredential;
import com.aspire.asat.phishing.repository.ProviderCredentialRepository;
import com.aspire.asat.phishing.service.ProviderCredentialService;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of {@link ProviderCredentialService}. All operations are
 * scoped to the current client resolved from {@link UserCurrentContextService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProviderCredentialServiceImpl implements ProviderCredentialService {

    private static final Set<String> SORT_FIELDS = Set.of("providerName", "category", "createdAt", "updatedAt");

    private final ProviderCredentialRepository providerCredentialRepository;
    private final ProviderCredentialMapper providerCredentialMapper;
    private final CredentialEncryptionService credentialEncryptionService;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    @Transactional
    public ProviderCredentialDto createProviderCredential(ProviderCredentialCreateRequest request) {
        String clientId = currentClientId();
        String providerName = normalize(request.getProviderName());

        if (providerCredentialRepository.existsByClientIdAndProviderName(clientId, providerName)) {
            throw new DuplicateDataFoundException("Provider credential already exists for: " + providerName);
        }

        ProviderCredential entity = providerCredentialMapper.toEntity(request, clientId);
        validateVoiceCloningBaseUrl(entity);
        encryptSecrets(entity);

        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            clearDefaults(clientId, entity.getCategory(), null);
        }

        ProviderCredential saved = providerCredentialRepository.save(entity);
        log.info("Created provider credential id={} provider={} client={}", saved.getId(), providerName, clientId);
        return toDto(saved);
    }

    @Override
    @Transactional
    public ProviderCredentialDto updateProviderCredential(String id, ProviderCredentialUpdateRequest request) {
        String clientId = currentClientId();
        ProviderCredential entity = requireOwned(id, clientId);

        String providerName = normalize(request.getProviderName());
        if (providerCredentialRepository.existsByClientIdAndProviderNameAndIdNot(clientId, providerName, id)) {
            throw new DuplicateDataFoundException("Provider credential already exists for: " + providerName);
        }

        providerCredentialMapper.updateFromRequest(entity, request);
        validateVoiceCloningBaseUrl(entity);
        encryptSecrets(entity);

        if (Boolean.TRUE.equals(entity.getIsDefault())) {
            clearDefaults(clientId, entity.getCategory(), id);
        }

        ProviderCredential saved = providerCredentialRepository.save(entity);
        log.info("Updated provider credential id={} client={}", id, clientId);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteProviderCredential(String id) {
        String clientId = currentClientId();
        ProviderCredential entity = requireOwned(id, clientId);
        providerCredentialRepository.delete(entity);
        log.info("Deleted provider credential id={} client={}", id, clientId);
    }

    @Override
    public ProviderCredentialDto getProviderCredentialById(String id) {
        String clientId = currentClientId();
        return toDto(requireOwned(id, clientId));
    }

    @Override
    public Page<ProviderCredentialDto> getProviderCredentials(
            String providerName, Boolean isActive, int offset, int pageSize, String sortBy, String sortOrder) {
        String clientId = currentClientId();
        Pageable pageable = pageable(offset, pageSize, sortBy, sortOrder);
        String provider = StringUtils.hasText(providerName) ? providerName.trim() : null;

        Page<ProviderCredential> page;
        if (provider != null && isActive != null) {
            page = providerCredentialRepository
                    .findByClientIdAndProviderNameIgnoreCaseAndIsActive(clientId, provider, isActive, pageable);
        } else if (provider != null) {
            page = providerCredentialRepository.findByClientIdAndProviderNameIgnoreCase(clientId, provider, pageable);
        } else if (isActive != null) {
            page = providerCredentialRepository.findByClientIdAndIsActive(clientId, isActive, pageable);
        } else {
            page = providerCredentialRepository.findByClientId(clientId, pageable);
        }
        return page.map(this::toDto);
    }

    @Override
    @Transactional
    public ProviderCredentialDto setDefault(String id) {
        String clientId = currentClientId();
        ProviderCredential entity = requireOwned(id, clientId);
        clearDefaults(clientId, entity.getCategory(), id);
        entity.setIsDefault(true);
        entity.setIsActive(true);
        ProviderCredential saved = providerCredentialRepository.save(entity);
        log.info("Set default provider credential id={} category={} client={}", id, entity.getCategory(), clientId);
        return toDto(saved);
    }

    @Override
    @Transactional
    public ProviderCredentialDto setActive(String id, boolean active) {
        String clientId = currentClientId();
        ProviderCredential entity = requireOwned(id, clientId);
        entity.setIsActive(active);
        if (!active) {
            entity.setIsDefault(false);
        }
        ProviderCredential saved = providerCredentialRepository.save(entity);
        log.info("Set active={} provider credential id={} client={}", active, id, clientId);
        return toDto(saved);
    }

    private void clearDefaults(String clientId, ProviderCategory category, String keepId) {
        if (category == null) {
            return;
        }
        List<ProviderCredential> categoryCreds = providerCredentialRepository
                .findByClientIdAndCategory(clientId, category);
        List<ProviderCredential> toSave = new ArrayList<>();
        for (ProviderCredential cred : categoryCreds) {
            if ((keepId == null || !keepId.equals(cred.getId())) && Boolean.TRUE.equals(cred.getIsDefault())) {
                cred.setIsDefault(false);
                toSave.add(cred);
            }
        }
        if (!toSave.isEmpty()) {
            providerCredentialRepository.saveAll(toSave);
        }
    }

    private void encryptSecrets(ProviderCredential entity) {
        if (StringUtils.hasText(entity.getApiKey()) && !credentialEncryptionService.isEncrypted(entity.getApiKey())) {
            entity.setApiKey(credentialEncryptionService.encrypt(entity.getApiKey()));
        }
        if (StringUtils.hasText(entity.getApiSecret()) && !credentialEncryptionService.isEncrypted(entity.getApiSecret())) {
            entity.setApiSecret(credentialEncryptionService.encrypt(entity.getApiSecret()));
        }
    }

    private ProviderCredentialDto toDto(ProviderCredential entity) {
        String decryptedApiKey = StringUtils.hasText(entity.getApiKey())
                ? credentialEncryptionService.decrypt(entity.getApiKey())
                : null;
        return providerCredentialMapper.toDto(entity, decryptedApiKey);
    }

    private ProviderCredential requireOwned(String id, String clientId) {
        return providerCredentialRepository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider credential not found"));
    }

    private Pageable pageable(int offset, int pageSize, String sortBy, String sortOrder) {
        int effectivePageSize = pageSize > 0 ? pageSize : 10;
        int pageNumber = Math.max(0, offset);
        String field = SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortOrder != null ? sortOrder : "desc");
        } catch (IllegalArgumentException e) {
            direction = Sort.Direction.DESC;
        }
        return PageRequest.of(pageNumber, effectivePageSize, Sort.by(direction, field));
    }

    private String currentClientId() {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        if (!StringUtils.hasText(clientId)) {
            throw new ResourceNotFoundException("Unauthorized resource access");
        }
        return clientId;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private void validateVoiceCloningBaseUrl(ProviderCredential entity) {
        if (entity.getCategory() != ProviderCategory.VOICE_CLONING) {
            return;
        }
        VoiceCloneAdapterFactory.resolveProviderFromBaseUrl(entity.getBaseUrl());
    }
}
