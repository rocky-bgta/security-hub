package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.ExpectedUserActionCreateRequest;
import com.aspire.asat.phishing.dto.request.ExpectedUserActionUpdateRequest;
import com.aspire.asat.phishing.dto.response.ExpectedUserActionDto;

import java.util.List;

/**
 * Service for configurable expected user action catalog (Mongo).
 */
public interface ExpectedUserActionService {

    ExpectedUserActionDto createExpectedUserAction(ExpectedUserActionCreateRequest request);

    ExpectedUserActionDto updateExpectedUserAction(String id, ExpectedUserActionUpdateRequest request);

    void deleteExpectedUserAction(String id);

    ExpectedUserActionDto getExpectedUserActionById(String id);

    List<ExpectedUserActionDto> getExpectedUserActions(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    long countExpectedUserActions(String searchParam, boolean isActive);
}
