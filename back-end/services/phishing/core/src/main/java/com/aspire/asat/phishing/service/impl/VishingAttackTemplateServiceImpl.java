package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.dto.request.VishingAttackTemplateCreateRequest;
import com.aspire.asat.phishing.dto.request.VishingAttackTemplateUpdateRequest;
import com.aspire.asat.phishing.dto.response.VishingAttackTemplateDto;
import com.aspire.asat.phishing.exception.DuplicateDataFoundException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.mapper.VishingAttackTemplateMapper;
import com.aspire.asat.phishing.model.VishingAttackTemplate;
import com.aspire.asat.phishing.repository.VishingAttackTemplateRepository;
import com.aspire.asat.phishing.service.VishingAttackTemplateService;
import com.aspire.asat.phishing.service.support.CatalogCrudSupport;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Platform catalog CRUD for vishing attack templates.
 * Writes restricted to ASPIRE_ADMIN / SUPER_ADMIN / SYSTEM_USER; reads open to all authenticated users.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VishingAttackTemplateServiceImpl implements VishingAttackTemplateService {

    private static final Set<String> SORT_FIELDS = Set.of("name", "createdAt");

    private final VishingAttackTemplateRepository repository;
    private final VishingAttackTemplateMapper mapper;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    @Transactional
    public VishingAttackTemplateDto create(VishingAttackTemplateCreateRequest request) {
        assertPlatformAdmin();
        String name = request.getName().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new DuplicateDataFoundException("Attack template name already exists");
        }
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        VishingAttackTemplate saved = repository.save(mapper.toEntity(request, context.getUserId()));
        log.info("Created VishingAttackTemplate id={}", saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public VishingAttackTemplateDto update(String id, VishingAttackTemplateUpdateRequest request) {
        assertPlatformAdmin();
        VishingAttackTemplate entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vishing attack template not found"));
        String name = request.getName().trim();
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateDataFoundException("Attack template name already exists");
        }
        mapper.applyUpdate(entity, request);
        VishingAttackTemplate saved = repository.save(entity);
        log.info("Updated VishingAttackTemplate id={}", id);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public void delete(String id) {
        assertPlatformAdmin();
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Vishing attack template not found");
        }
        repository.deleteById(id);
        log.info("Deleted VishingAttackTemplate id={}", id);
    }

    @Override
    public VishingAttackTemplateDto getById(String id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Vishing attack template not found"));
    }

    @Override
    public List<VishingAttackTemplateDto> list(
            String searchParam, int offset, int pageSize, String sortBy, String sortOrder) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        int effectivePageSize = pageSize > 0 ? pageSize : 10;
        int pageNumber = Math.max(0, offset);
        Pageable pageable = PageRequest.of(
                pageNumber, effectivePageSize, CatalogCrudSupport.sort(sortBy, sortOrder, SORT_FIELDS));

        Page<VishingAttackTemplate> pageResult = (search != null)
                ? repository.searchByName(CatalogCrudSupport.nameContainsPattern(search), pageable)
                : repository.findAll(pageable);

        return pageResult.getContent().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String searchParam) {
        String search = (searchParam != null && !searchParam.trim().isEmpty()) ? searchParam.trim() : null;
        if (search != null) {
            return repository.countSearchByName(CatalogCrudSupport.nameContainsPattern(search));
        }
        return repository.count();
    }

    private void assertPlatformAdmin() {
        UserType userType = getCurrentUserType();
        if (userType != UserType.SUPER_ADMIN
                && userType != UserType.ASPIRE_ADMIN
                && userType != UserType.SYSTEM_USER) {
            throw new ServiceException("You do not have permission to manage vishing attack templates");
        }
    }

    private UserType getCurrentUserType() {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        try {
            return UserType.fromString(context.getUserType());
        } catch (Exception e) {
            return null;
        }
    }
}
