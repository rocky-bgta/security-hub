package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.ConstraintsDataCreateRequest;
import com.aspire.asat.phishing.dto.request.ConstraintsDataUpdateRequest;
import com.aspire.asat.phishing.dto.response.ConstraintsDataDto;

import java.util.List;

/**
 * Service for configurable constraints data catalog (Mongo).
 */
public interface ConstraintsDataService {

    ConstraintsDataDto createConstraintsData(ConstraintsDataCreateRequest request);

    ConstraintsDataDto updateConstraintsData(String id, ConstraintsDataUpdateRequest request);

    void deleteConstraintsData(String id);

    ConstraintsDataDto getConstraintsDataById(String id);

    List<ConstraintsDataDto> getConstraintsDataList(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    long countConstraintsData(String searchParam, boolean isActive);
}
