package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.EmailTypeCreateRequest;
import com.aspire.asat.phishing.dto.response.EmailTypeDto;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.EmailType;
import com.aspire.asat.phishing.repository.EmailTypeRepository;
import com.aspire.asat.phishing.service.EmailTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for EmailType configuration entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTypeServiceImpl implements EmailTypeService {

    private final EmailTypeRepository emailTypeRepository;

    @Override
    @Transactional
    public EmailTypeDto createEmailType(EmailTypeCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Email type request is required");
        }

        boolean isActive = request.getIsActive() != null ? request.getIsActive() : true;
        Instant now = Instant.now();

        EmailType saved = emailTypeRepository.save(EmailType.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName() != null ? request.getName().trim() : null)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .isActive(isActive)
                .createdAt(now)
                .build());

        log.info("Created EmailType id={}", saved.getId());
        return toDto(saved);
    }

    @Override
    public EmailTypeDto getEmailTypeById(String emailTypeId) {
        return emailTypeRepository.findById(emailTypeId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Email type not found"));
    }

    @Override
    public List<EmailTypeDto> getEmailTypes(String searchParam, boolean isActive,
                                              int offset, int pageSize,
                                              String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty())
                ? searchParam.trim()
                : null;

        Pageable pageable = buildPageable(offset, pageSize, sortBy, sortOrder);

        Page<EmailType> pageResult = (search != null)
                ? emailTypeRepository.searchByNameAndIsActive(search, isActive, pageable)
                : emailTypeRepository.findByIsActive(isActive, pageable);

        return pageResult.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countEmailTypes(String searchParam, boolean isActive) {
        String search = (searchParam != null && !searchParam.trim().isEmpty())
                ? searchParam.trim()
                : null;

        if (search != null) {
            return emailTypeRepository.countSearchByNameAndIsActive(search, isActive);
        }
        return emailTypeRepository.countByIsActive(isActive);
    }

    private Pageable buildPageable(int offset, int pageSize, String sortBy, String sortOrder) {
        int effectivePageSize = pageSize > 0 ? pageSize : 10;
        int page = effectivePageSize > 0 ? Math.max(0, offset) / effectivePageSize : 0;

        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortField);

        return PageRequest.of(page, effectivePageSize, sort);
    }

    private EmailTypeDto toDto(EmailType entity) {
        if (entity == null) {
            return null;
        }
        return EmailTypeDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

