package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.PersonalizationLevelCreateRequest;
import com.aspire.asat.phishing.dto.request.PersonalizationLevelUpdateRequest;
import com.aspire.asat.phishing.dto.response.PersonalizationLevelDto;

import java.util.List;

/**
 * Service for configurable personalization level catalog (Mongo).
 */
public interface PersonalizationLevelService {

    PersonalizationLevelDto createPersonalizationLevel(PersonalizationLevelCreateRequest request);

    PersonalizationLevelDto updatePersonalizationLevel(String id, PersonalizationLevelUpdateRequest request);

    void deletePersonalizationLevel(String id);

    PersonalizationLevelDto getPersonalizationLevelById(String id);

    List<PersonalizationLevelDto> getPersonalizationLevels(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    long countPersonalizationLevels(String searchParam, boolean isActive);
}
