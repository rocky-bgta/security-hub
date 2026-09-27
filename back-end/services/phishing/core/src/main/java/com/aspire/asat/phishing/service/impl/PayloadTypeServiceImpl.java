package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.PayloadTypeChannel;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.dto.request.PayloadTypeCreateRequest;
import com.aspire.asat.phishing.dto.request.PayloadTypeUpdateRequest;
import com.aspire.asat.phishing.dto.response.PayloadTypeDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.PayloadType;
import com.aspire.asat.phishing.repository.PayloadTypeRepository;
import com.aspire.asat.phishing.service.PayloadTypeService;
import com.aspire.asat.phishing.service.support.CatalogCrudSupport;
import com.aspire.asat.phishing.service.support.PayloadTypeChannelSupport;
import com.aspire.asat.phishing.utils.CatalogDtoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for configurable payload type entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PayloadTypeServiceImpl implements PayloadTypeService {

    private final PayloadTypeRepository payloadTypeRepository;

    @Override
    @Transactional
    public PayloadTypeDto createPayloadType(PayloadTypeCreateRequest request) {
        String name = request.getName().trim();
        PayloadTypeChannel channel = PayloadTypeChannelSupport.effectiveChannel(request.getChannel());
        if (nameExistsForChannel(name, channel, null)) {
            throw new DuplicateDataFoundException("Payload type name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(null, channel);
        }
        boolean active = request.getIsActive() == null || Boolean.TRUE.equals(request.getIsActive());
        PayloadType entity = PayloadType.builder()
                .name(name)
                .description(CatalogCrudSupport.trimOrNull(request.getDescription()))
                .displayOrder(request.getDisplayOrder())
                .isDefault(Boolean.TRUE.equals(request.getIsDefault()))
                .isActive(active)
                .channel(channel)
                .build();
        PayloadType saved = payloadTypeRepository.save(entity);
        log.info("Created PayloadType id={} channel={}", saved.getId(), channel);
        return toDto(saved);
    }

    @Override
    @Transactional
    public PayloadTypeDto updatePayloadType(String id, PayloadTypeUpdateRequest request) {
        PayloadType entity = payloadTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payload type not found"));
        String name = request.getName().trim();
        PayloadTypeChannel channel = PayloadTypeChannelSupport.effectiveChannelForUpdate(
                request.getChannel(), entity.getChannel());
        if (nameExistsForChannel(name, channel, id)) {
            throw new DuplicateDataFoundException("Payload type name already exists");
        }
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultsExcluding(id, channel);
        }
        entity.setName(name);
        entity.setDescription(CatalogCrudSupport.trimOrNull(request.getDescription()));
        entity.setDisplayOrder(request.getDisplayOrder());
        entity.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));
        entity.setIsActive(Boolean.TRUE.equals(request.getIsActive()));
        entity.setChannel(channel);
        PayloadType saved = payloadTypeRepository.save(entity);
        log.info("Updated PayloadType id={} channel={}", id, channel);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deletePayloadType(String id) {
        if (!payloadTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payload type not found");
        }
        payloadTypeRepository.deleteById(id);
        log.info("Deleted PayloadType id={}", id);
    }

    @Override
    public PayloadTypeDto getPayloadTypeById(String payloadTypeId) {
        return payloadTypeRepository.findById(payloadTypeId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Payload type not found"));
    }

    @Override
    public List<PayloadTypeDto> getPayloadTypes(String searchParam, boolean isActive, int offset, int pageSize,
                                                String sortBy, String sortOrder, PayloadTypeChannel channel) {
        PayloadTypeChannel effectiveChannel = PayloadTypeChannelSupport.effectiveChannel(channel);
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        Pageable pageable = CatalogCrudSupport.pageableForOffsetAsPageIndex(offset, pageSize, sortBy, sortOrder);

        Page<PayloadType> pageResult = queryPage(effectiveChannel, isActive, search, pageable);

        return pageResult.getContent().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public long countPayloadTypes(String searchParam, boolean isActive, PayloadTypeChannel channel) {
        PayloadTypeChannel effectiveChannel = PayloadTypeChannelSupport.effectiveChannel(channel);
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            String pattern = CatalogCrudSupport.nameContainsPattern(search);
            return countSearch(effectiveChannel, isActive, pattern);
        }
        return countAll(effectiveChannel, isActive);
    }

    @Override
    public void validatePayloadTypeForTemplate(TemplateType templateType, PayloadTypeDto payloadType) {
        String payloadTypeId = CatalogDtoUtils.getId(payloadType);
        if (!StringUtils.hasText(payloadTypeId)) {
            return;
        }
        PayloadTypeChannel expectedChannel = PayloadTypeChannelSupport.fromTemplateType(
                templateType != null ? templateType : TemplateType.EMAIL);
        PayloadTypeDto catalogEntry = getPayloadTypeById(payloadTypeId.trim());
        PayloadTypeChannel actualChannel = PayloadTypeChannelSupport.effectiveChannelFromEntity(catalogEntry.getChannel());
        if (actualChannel != expectedChannel) {
            throw new PhishingValidationException(
                    "Payload type channel " + actualChannel + " does not match template type "
                            + (templateType != null ? templateType : TemplateType.EMAIL));
        }
    }

    private Page<PayloadType> queryPage(PayloadTypeChannel channel, boolean isActive, String search,
                                        Pageable pageable) {
        return switch (channel) {
            case SMS -> querySmsPage(isActive, search, pageable);
            case VOICE -> queryVoicePage(isActive, search, pageable);
            case EMAIL -> queryEmailPage(isActive, search, pageable);
        };
    }

    private Page<PayloadType> queryEmailPage(boolean isActive, String search, Pageable pageable) {
        if (isActive) {
            return search != null
                    ? payloadTypeRepository.searchByNameWhereEffectiveActiveByEmailChannel(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : payloadTypeRepository.findWhereEffectiveActiveByEmailChannel(pageable);
        }
        return search != null
                ? payloadTypeRepository.searchByNameWhereInactiveByEmailChannel(
                        CatalogCrudSupport.nameContainsPattern(search), pageable)
                : payloadTypeRepository.findWhereInactiveByEmailChannel(pageable);
    }

    private Page<PayloadType> querySmsPage(boolean isActive, String search, Pageable pageable) {
        if (isActive) {
            return search != null
                    ? payloadTypeRepository.searchByNameWhereEffectiveActiveBySmsChannel(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : payloadTypeRepository.findWhereEffectiveActiveBySmsChannel(pageable);
        }
        return search != null
                ? payloadTypeRepository.searchByNameWhereInactiveBySmsChannel(
                        CatalogCrudSupport.nameContainsPattern(search), pageable)
                : payloadTypeRepository.findWhereInactiveBySmsChannel(pageable);
    }

    private Page<PayloadType> queryVoicePage(boolean isActive, String search, Pageable pageable) {
        if (isActive) {
            return search != null
                    ? payloadTypeRepository.searchByNameWhereEffectiveActiveByVoiceChannel(
                            CatalogCrudSupport.nameContainsPattern(search), pageable)
                    : payloadTypeRepository.findWhereEffectiveActiveByVoiceChannel(pageable);
        }
        return search != null
                ? payloadTypeRepository.searchByNameWhereInactiveByVoiceChannel(
                        CatalogCrudSupport.nameContainsPattern(search), pageable)
                : payloadTypeRepository.findWhereInactiveByVoiceChannel(pageable);
    }

    private long countSearch(PayloadTypeChannel channel, boolean isActive, String pattern) {
        return switch (channel) {
            case SMS -> isActive
                    ? payloadTypeRepository.countSearchByNameWhereEffectiveActiveBySmsChannel(pattern)
                    : payloadTypeRepository.countSearchByNameWhereInactiveBySmsChannel(pattern);
            case VOICE -> isActive
                    ? payloadTypeRepository.countSearchByNameWhereEffectiveActiveByVoiceChannel(pattern)
                    : payloadTypeRepository.countSearchByNameWhereInactiveByVoiceChannel(pattern);
            case EMAIL -> isActive
                    ? payloadTypeRepository.countSearchByNameWhereEffectiveActiveByEmailChannel(pattern)
                    : payloadTypeRepository.countSearchByNameWhereInactiveByEmailChannel(pattern);
        };
    }

    private long countAll(PayloadTypeChannel channel, boolean isActive) {
        return switch (channel) {
            case SMS -> isActive
                    ? payloadTypeRepository.countWhereEffectiveActiveBySmsChannel()
                    : payloadTypeRepository.countWhereInactiveBySmsChannel();
            case VOICE -> isActive
                    ? payloadTypeRepository.countWhereEffectiveActiveByVoiceChannel()
                    : payloadTypeRepository.countWhereInactiveByVoiceChannel();
            case EMAIL -> isActive
                    ? payloadTypeRepository.countWhereEffectiveActiveByEmailChannel()
                    : payloadTypeRepository.countWhereInactiveByEmailChannel();
        };
    }

    private boolean nameExistsForChannel(String name, PayloadTypeChannel channel, String excludeId) {
        String pattern = CatalogCrudSupport.exactNamePattern(name);
        return switch (channel) {
            case SMS -> excludeId == null
                    ? payloadTypeRepository.existsByNameIgnoreCaseAndSmsChannel(pattern)
                    : payloadTypeRepository.existsByNameIgnoreCaseAndSmsChannelAndIdNot(pattern, excludeId);
            case VOICE -> excludeId == null
                    ? payloadTypeRepository.existsByNameIgnoreCaseAndVoiceChannel(pattern)
                    : payloadTypeRepository.existsByNameIgnoreCaseAndVoiceChannelAndIdNot(pattern, excludeId);
            case EMAIL -> excludeId == null
                    ? payloadTypeRepository.existsByNameIgnoreCaseAndEmailChannel(pattern)
                    : payloadTypeRepository.existsByNameIgnoreCaseAndEmailChannelAndIdNot(pattern, excludeId);
        };
    }

    private void clearDefaultsExcluding(String keepId, PayloadTypeChannel channel) {
        List<PayloadType> defaults = switch (channel) {
            case SMS -> payloadTypeRepository.findAllByIsDefaultTrueAndSmsChannel();
            case VOICE -> payloadTypeRepository.findAllByIsDefaultTrueAndVoiceChannel();
            case EMAIL -> payloadTypeRepository.findAllByIsDefaultTrueAndEmailChannel();
        };
        List<PayloadType> toSave = new ArrayList<>();
        for (PayloadType payloadType : defaults) {
            if (keepId == null || !keepId.equals(payloadType.getId())) {
                payloadType.setIsDefault(false);
                toSave.add(payloadType);
            }
        }
        if (!toSave.isEmpty()) {
            payloadTypeRepository.saveAll(toSave);
        }
    }

    private PayloadTypeDto toDto(PayloadType entity) {
        if (entity == null) {
            return null;
        }
        Integer order = entity.getDisplayOrder() != null ? entity.getDisplayOrder() : 0;
        Boolean def = entity.getIsDefault() != null ? entity.getIsDefault() : Boolean.FALSE;
        Boolean active = entity.getIsActive() != null ? entity.getIsActive() : Boolean.TRUE;
        return PayloadTypeDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .displayOrder(order)
                .isDefault(def)
                .isActive(active)
                .channel(PayloadTypeChannelSupport.effectiveChannelFromEntity(entity.getChannel()))
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
