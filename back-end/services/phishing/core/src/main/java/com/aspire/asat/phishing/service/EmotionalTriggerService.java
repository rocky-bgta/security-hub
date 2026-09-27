package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.EmotionalTriggerCreateRequest;
import com.aspire.asat.phishing.dto.request.EmotionalTriggerUpdateRequest;
import com.aspire.asat.phishing.dto.response.EmotionalTriggerDto;

import java.util.List;

/**
 * Service for configurable emotional trigger catalog (Mongo).
 */
public interface EmotionalTriggerService {

    EmotionalTriggerDto createEmotionalTrigger(EmotionalTriggerCreateRequest request);

    EmotionalTriggerDto updateEmotionalTrigger(String id, EmotionalTriggerUpdateRequest request);

    void deleteEmotionalTrigger(String id);

    EmotionalTriggerDto getEmotionalTriggerById(String id);

    List<EmotionalTriggerDto> getEmotionalTriggers(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    long countEmotionalTriggers(String searchParam, boolean isActive);
}
