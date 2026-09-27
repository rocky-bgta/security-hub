package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.VishingScenarioStatus;
import com.aspire.asat.phishing.dto.request.VishingScenarioCreateRequest;
import com.aspire.asat.phishing.dto.request.VishingScenarioUpdateRequest;
import com.aspire.asat.phishing.dto.response.VishingScenarioDto;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.mapper.VishingScenarioMapper;
import com.aspire.asat.phishing.model.VishingScenario;
import com.aspire.asat.phishing.repository.VishingScenarioRepository;
import com.aspire.asat.phishing.service.VishingScenarioService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VishingScenarioServiceImpl implements VishingScenarioService {

    private final VishingScenarioRepository repository;
    private final VishingScenarioMapper mapper;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public List<VishingScenarioDto> getScenarios(int offset, int pageSize, String searchParam) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        Pageable pageable = PageRequest.of(Math.max(0, offset), Math.max(1, pageSize),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<VishingScenario> page;
        if (searchParam != null && !searchParam.trim().isEmpty()) {
            page = repository.searchByClientIdAndKeyword(clientId, searchParam.trim(), pageable);
        } else {
            page = repository.findByClientIdOrGlobal(clientId, pageable);
        }
        return page.getContent().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    @Override
    public long countScenarios(String searchParam) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        Pageable pageable = PageRequest.of(0, 1);
        Page<VishingScenario> page;
        if (searchParam != null && !searchParam.trim().isEmpty()) {
            page = repository.searchByClientIdAndKeyword(clientId, searchParam.trim(), pageable);
        } else {
            page = repository.findByClientIdOrGlobal(clientId, pageable);
        }
        return page.getTotalElements();
    }

    @Override
    public VishingScenarioDto getById(String id) {
        return mapper.toDto(findAccessible(id));
    }

    @Override
    @Transactional
    public VishingScenarioDto create(VishingScenarioCreateRequest request) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        if (repository.existsByClientIdAndScenarioName(clientId, request.getScenarioName().trim())) {
            throw new PhishingValidationException("Scenario name already exists");
        }
        VishingScenario saved = repository.save(mapper.toEntity(request, clientId));
        return mapper.toDto(saved);
    }

    @Override
    @Transactional
    public VishingScenarioDto update(String id, VishingScenarioUpdateRequest request) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        VishingScenario existing = findOwned(id, clientId);
        if (repository.existsByClientIdAndScenarioNameAndIdNot(clientId, request.getScenarioName().trim(), id)) {
            throw new PhishingValidationException("Scenario name already exists");
        }
        mapper.applyUpdate(existing, request);
        return mapper.toDto(repository.save(existing));
    }

    @Override
    @Transactional
    public void delete(String id) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        VishingScenario existing = findOwned(id, clientId);
        if (existing.isGlobal()) {
            throw new PhishingValidationException("Global scenarios cannot be deleted");
        }
        repository.delete(existing);
    }

    @Override
    @Transactional
    public VishingScenarioDto publish(String id) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        VishingScenario existing = findOwned(id, clientId);
        existing.setStatus(VishingScenarioStatus.PUBLISHED);
        return mapper.toDto(repository.save(existing));
    }

    VishingScenario findAccessible(String id) {
        String clientId = userCurrentContextService.getCurrentUserContext().getClientAdminId();
        return repository.findByIdAndClientIdOrGlobal(id, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Vishing scenario not found"));
    }

    private VishingScenario findOwned(String id, String clientId) {
        return repository.findByIdAndClientId(id, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Vishing scenario not found"));
    }
}
