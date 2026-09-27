package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.TriggerEventCreateRequest;
import com.aspire.asat.phishing.dto.request.TriggerEventUpdateRequest;
import com.aspire.asat.phishing.dto.response.TriggerEventDto;

import java.util.List;

/**
 * Service for configurable trigger event catalog (Mongo).
 */
public interface TriggerEventService {

    TriggerEventDto createTriggerEvent(TriggerEventCreateRequest request);

    TriggerEventDto updateTriggerEvent(String id, TriggerEventUpdateRequest request);

    void deleteTriggerEvent(String id);

    TriggerEventDto getTriggerEventById(String id);

    List<TriggerEventDto> getTriggerEvents(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    long countTriggerEvents(String searchParam, boolean isActive);
}
