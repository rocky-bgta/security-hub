package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.DeceptionLevelCreateRequest;
import com.aspire.asat.phishing.dto.request.DeceptionLevelUpdateRequest;
import com.aspire.asat.phishing.dto.response.DeceptionLevelDto;

import java.util.List;

/**
 * Service for configurable deception level catalog (Mongo).
 */
public interface DeceptionLevelService {

    DeceptionLevelDto createDeceptionLevel(DeceptionLevelCreateRequest request);

    DeceptionLevelDto updateDeceptionLevel(String id, DeceptionLevelUpdateRequest request);

    void deleteDeceptionLevel(String id);

    DeceptionLevelDto getDeceptionLevelById(String id);

    List<DeceptionLevelDto> getDeceptionLevels(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    long countDeceptionLevels(String searchParam, boolean isActive);
}
