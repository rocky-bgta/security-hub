package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.basePackage.BasePackageConfigRequest;
import com.aspire.asat.cms.dto.basePackage.BasePackageConfigResponse;
import com.aspire.asat.cms.dto.basePackage.BasePackageConfigUpdateRequest;
import com.aspire.asat.cms.exception.DuplicateNameException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.BasePackageConfig;
import com.aspire.asat.cms.repository.BasePackageConfigRepository;
import com.aspire.asat.cms.service.BasePackageConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasePackageConfigServiceImpl implements BasePackageConfigService {
    private final BasePackageConfigRepository basePackageConfigRepository;

    private static final String RESOURCE_NOT_FOUND_WITH_ID = "Base package config not found with id: ";
    private static final String CREATED_BY = "system";


    @Override
    public BasePackageConfigResponse createBasePackageConfig(BasePackageConfigRequest request) {
        log.info("Creating base package config with name: {}", request.getName());

        // Check if name already exists
        if (basePackageConfigRepository.existsByName(request.getName())) {
            throw new DuplicateNameException("Base package config with name '" + request.getName() + "' already exists");
        }

        String basePackageId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        BasePackageConfig basePackageConfig = BasePackageConfig.builder()
                .basePackageId(basePackageId)
                .name(request.getName())
                .active(true) // Always set to true for new records
                .createdAt(now)
                .updatedAt(now)
                .createdBy(CREATED_BY) // Default value since not provided in request
                .updatedBy(CREATED_BY) // Default value since not provided in request
                .build();

        BasePackageConfig savedConfig = basePackageConfigRepository.save(basePackageConfig);
        log.info("Base package config created successfully with id: {}", basePackageId);

        return mapToResponse(savedConfig);
    }

    @Override
    public List<BasePackageConfigResponse> getAllBasePackageConfigs() {
        log.info("Fetching all base package configs");
        List<BasePackageConfig> configs = basePackageConfigRepository.findAll();
        return configs.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public BasePackageConfigResponse getBasePackageConfigById(String basePackageId) {
        log.info("Fetching base package config by id: {}", basePackageId);
        BasePackageConfig config = basePackageConfigRepository.findByBasePackageId(basePackageId)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NOT_FOUND_WITH_ID + basePackageId));

        return mapToResponse(config);
    }

    @Override
    public BasePackageConfigResponse updateBasePackageConfig(String basePackageId, BasePackageConfigUpdateRequest request) {
        log.info("Updating base package config with id: {}", basePackageId);

        BasePackageConfig existingConfig = basePackageConfigRepository.findByBasePackageId(basePackageId)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NOT_FOUND_WITH_ID + basePackageId));

        // Check if name already exists for different record
        if (!existingConfig.getName().equals(request.getName()) &&
                basePackageConfigRepository.existsByNameAndBasePackageIdNot(request.getName(), basePackageId)) {
            throw new DuplicateNameException("Base package config with name '" + request.getName() + "' already exists");
        }

        existingConfig.setName(request.getName());
        existingConfig.setUpdatedAt(Instant.now());
        existingConfig.setUpdatedBy(CREATED_BY); // Default value since not provided in request

        BasePackageConfig updatedConfig = basePackageConfigRepository.save(existingConfig);
        log.info("Base package config updated successfully with id: {}", basePackageId);

        return mapToResponse(updatedConfig);
    }

    @Override
    public String deleteBasePackageConfig(String basePackageId) {
        log.info("Soft deleting base package config with id: {}", basePackageId);

        BasePackageConfig config = basePackageConfigRepository.findByBasePackageId(basePackageId)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NOT_FOUND_WITH_ID + basePackageId));

        // Soft delete by setting active to false
        config.setActive(false);
        config.setUpdatedAt(Instant.now());
        config.setUpdatedBy(CREATED_BY);

        basePackageConfigRepository.save(config);
        log.info("Base package config soft deleted successfully with id: {}", basePackageId);

        return "Base package config deleted successfully";
    }

    @Override
    public boolean existsByName(String name) {
        return basePackageConfigRepository.existsByName(name);
    }

    private BasePackageConfigResponse mapToResponse(BasePackageConfig config) {
        return BasePackageConfigResponse.builder()
                .basePackageId(config.getBasePackageId())
                .name(config.getName())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .createdBy(config.getCreatedBy())
                .updatedBy(config.getUpdatedBy())
                .active(config.getActive())
                .build();
    }
}
