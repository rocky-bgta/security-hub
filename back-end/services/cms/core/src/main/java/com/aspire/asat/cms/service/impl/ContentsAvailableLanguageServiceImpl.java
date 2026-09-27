package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.language.ContentsAvailableLanguageReqDto;
import com.aspire.asat.cms.dto.language.ContentsAvailableLanguageRespDto;
import com.aspire.asat.cms.exception.DuplicateDataFoundException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.ContentsAvailableLanguage;
import com.aspire.asat.cms.repository.ContentsAvailableLanguageRepository;
import com.aspire.asat.cms.service.ContentsAvailableLanguageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContentsAvailableLanguageServiceImpl implements ContentsAvailableLanguageService {

    private final ContentsAvailableLanguageRepository repository;

    @Override
    public ContentsAvailableLanguageRespDto create(ContentsAvailableLanguageReqDto dto) {
        log.info("Creating contents available language: {}", dto);
        if (repository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new DuplicateDataFoundException("Language with code '" + dto.getCode() + "' already exists.");
        }
        ContentsAvailableLanguage entity = ContentsAvailableLanguage.toEntity(dto);
        return ContentsAvailableLanguage.toRespDto(repository.save(entity));
    }

    @Override
    public ContentsAvailableLanguageRespDto update(String id, ContentsAvailableLanguageReqDto dto) {
        log.info("Updating contents available language id: {}", id);
        ContentsAvailableLanguage existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language with id '" + id + "' not found."));

        if (!existing.getCode().equalsIgnoreCase(dto.getCode())
                && repository.existsByCodeIgnoreCase(dto.getCode())) {
            throw new DuplicateDataFoundException("Language with code '" + dto.getCode() + "' already exists.");
        }

        existing.setLanguageName(dto.getLanguageName());
        existing.setCode(dto.getCode());
        existing.setActive(dto.isActive());
        existing.setUpdatedAt(Instant.now());

        return ContentsAvailableLanguage.toRespDto(repository.save(existing));
    }

    @Override
    public ContentsAvailableLanguageRespDto getById(String id) {
        log.info("Fetching contents available language by id: {}", id);
        ContentsAvailableLanguage entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language with id '" + id + "' not found."));
        return ContentsAvailableLanguage.toRespDto(entity);
    }

    @Override
    public void deleteById(String id) {
        log.info("Deleting contents available language id: {}", id);
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Language with id '" + id + "' not found.");
        }
        repository.deleteById(id);
    }

    @Override
    public List<ContentsAvailableLanguageRespDto> getAll(Boolean active) {
        log.info("Fetching contents available languages, active filter: {}", active);
        List<ContentsAvailableLanguage> entities = Boolean.TRUE.equals(active)
                ? repository.findByActiveTrue()
                : repository.findAll(Sort.by(Sort.Direction.ASC, "languageName"));
        return entities.stream()
                .map(ContentsAvailableLanguage::toRespDto)
                .toList();
    }
}
