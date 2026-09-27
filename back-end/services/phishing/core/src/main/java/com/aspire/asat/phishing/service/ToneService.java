package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.ToneCreateRequest;
import com.aspire.asat.phishing.dto.request.ToneUpdateRequest;
import com.aspire.asat.phishing.dto.response.ToneDto;

import java.util.List;

/**
 * Service for configurable tone catalog (Mongo).
 */
public interface ToneService {

    ToneDto createTone(ToneCreateRequest request);

    ToneDto updateTone(String id, ToneUpdateRequest request);

    void deleteTone(String id);

    ToneDto getToneById(String toneId);

    List<ToneDto> getTones(String searchParam, boolean isActive, int offset, int pageSize,
                           String sortBy, String sortOrder);

    long countTones(String searchParam, boolean isActive);
}
