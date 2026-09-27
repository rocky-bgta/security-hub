package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.CallToActionCreateRequest;
import com.aspire.asat.phishing.dto.request.CallToActionUpdateRequest;
import com.aspire.asat.phishing.dto.response.CallToActionDto;

import java.util.List;

/**
 * Service for configurable call-to-action catalog (Mongo).
 */
public interface CallToActionService {

    CallToActionDto createCallToAction(CallToActionCreateRequest request);

    CallToActionDto updateCallToAction(String id, CallToActionUpdateRequest request);

    void deleteCallToAction(String id);

    CallToActionDto getCallToActionById(String id);

    List<CallToActionDto> getCallToActions(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    long countCallToActions(String searchParam, boolean isActive);
}
