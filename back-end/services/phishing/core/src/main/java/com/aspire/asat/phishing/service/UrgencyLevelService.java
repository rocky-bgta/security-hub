package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.UrgencyLevelCreateRequest;
import com.aspire.asat.phishing.dto.request.UrgencyLevelUpdateRequest;
import com.aspire.asat.phishing.dto.response.UrgencyLevelDto;

import java.util.List;

/**
 * Service for configurable urgency level catalog (Mongo).
 */
public interface UrgencyLevelService {

    UrgencyLevelDto createUrgencyLevel(UrgencyLevelCreateRequest request);

    UrgencyLevelDto updateUrgencyLevel(String id, UrgencyLevelUpdateRequest request);

    void deleteUrgencyLevel(String id);

    UrgencyLevelDto getUrgencyLevelById(String id);

    List<UrgencyLevelDto> getUrgencyLevels(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    long countUrgencyLevels(String searchParam, boolean isActive);
}
