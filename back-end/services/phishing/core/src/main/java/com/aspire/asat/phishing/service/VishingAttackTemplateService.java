package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.VishingAttackTemplateCreateRequest;
import com.aspire.asat.phishing.dto.request.VishingAttackTemplateUpdateRequest;
import com.aspire.asat.phishing.dto.response.VishingAttackTemplateDto;

import java.util.List;

/**
 * Service for platform-managed vishing attack template catalog.
 */
public interface VishingAttackTemplateService {

    VishingAttackTemplateDto create(VishingAttackTemplateCreateRequest request);

    VishingAttackTemplateDto update(String id, VishingAttackTemplateUpdateRequest request);

    void delete(String id);

    VishingAttackTemplateDto getById(String id);

    List<VishingAttackTemplateDto> list(String searchParam, int offset, int pageSize, String sortBy, String sortOrder);

    long count(String searchParam);
}
