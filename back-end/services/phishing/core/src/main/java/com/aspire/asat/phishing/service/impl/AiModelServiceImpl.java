package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.dto.request.AiModelCreateRequest;
import com.aspire.asat.phishing.dto.request.AiModelUpdateRequest;
import com.aspire.asat.phishing.dto.response.AiModelDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.AiModel;
import com.aspire.asat.phishing.repository.AiModelRepository;
import com.aspire.asat.phishing.service.AiModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiModelServiceImpl implements AiModelService {

    private final AiModelRepository aiModelRepository;

    @Override
    @Transactional
    public AiModelDto create(AiModelCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiModelCreateRequest is required");
        }

        AiProviderType providerType = request.getProviderType();
        if (request.isDefault()) {
            clearDefaultForProvider(providerType);
        }

        Instant now = Instant.now();
        AiModel saved = aiModelRepository.save(AiModel.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName() != null ? request.getName().trim() : null)
                .providerType(providerType)
                .isDefault(request.isDefault())
                .isActive(request.isActive())
                .createdAt(now)
                .build());

        log.info("Created AiModel id={} provider={}", saved.getId(), providerType);
        return toDto(saved);
    }

    private void clearDefaultForProvider(AiProviderType providerType) {
        List<AiModel> existing = aiModelRepository.findByProviderTypeAndIsDefaultTrue(providerType);
        for (AiModel m : existing) {
            m.setDefault(false);
            aiModelRepository.save(m);
        }
    }

    private void clearDefaultForProviderExcept(AiProviderType providerType, String exceptModelId) {
        List<AiModel> existing = aiModelRepository.findByProviderTypeAndIsDefaultTrue(providerType);
        for (AiModel m : existing) {
            if (exceptModelId == null || !exceptModelId.equals(m.getId())) {
                m.setDefault(false);
                aiModelRepository.save(m);
            }
        }
    }

    @Override
    public AiModelDto getById(String id) {
        return aiModelRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("AI model not found"));
    }

    @Override
    @Transactional
    public AiModelDto update(String id, AiModelUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiModelUpdateRequest is required");
        }
        AiModel entity = aiModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AI model not found"));

        AiProviderType newProvider = request.getProviderType();
        if (entity.isDefault()) {
            clearDefaultForProviderExcept(newProvider, id);
        }

        entity.setName(request.getName() != null ? request.getName().trim() : null);
        entity.setProviderType(newProvider);

        AiModel saved = aiModelRepository.save(entity);
        log.info("Updated AiModel id={} provider={}", saved.getId(), newProvider);
        return toDto(saved);
    }

    @Override
    public List<AiModelDto> list(AiProviderType providerType) {
        List<AiModel> rows = providerType == null
                ? aiModelRepository.findAllByOrderByProviderTypeAscNameAsc()
                : aiModelRepository.findByProviderTypeOrderByNameAsc(providerType);
        return rows.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        if (!aiModelRepository.existsById(id)) {
            throw new ResourceNotFoundException("AI model not found");
        }
        aiModelRepository.deleteById(id);
        log.info("Deleted AiModel id={}", id);
    }

    private AiModelDto toDto(AiModel entity) {
        if (entity == null) {
            return null;
        }
        return AiModelDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .providerType(entity.getProviderType())
                .isDefault(entity.isDefault())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
