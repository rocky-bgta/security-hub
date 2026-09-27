package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.DifficultyCreateRequest;
import com.aspire.asat.phishing.dto.request.DifficultyUpdateRequest;
import com.aspire.asat.phishing.dto.response.DifficultyDto;

import java.util.List;

/**
 * Service for configurable difficulty catalog (Mongo).
 */
public interface DifficultyService {

    DifficultyDto createDifficulty(DifficultyCreateRequest request);

    DifficultyDto updateDifficulty(String id, DifficultyUpdateRequest request);

    void deleteDifficulty(String id);

    DifficultyDto getDifficultyById(String id);

    List<DifficultyDto> getDifficulties(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    long countDifficulties(String searchParam, boolean isActive);
}
