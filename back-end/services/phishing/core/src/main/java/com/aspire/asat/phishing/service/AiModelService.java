package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.aspire.asat.phishing.dto.request.AiModelCreateRequest;
import com.aspire.asat.phishing.dto.request.AiModelUpdateRequest;
import com.aspire.asat.phishing.dto.response.AiModelDto;

import java.util.List;

public interface AiModelService {

    AiModelDto create(AiModelCreateRequest request);

    AiModelDto getById(String id);

    AiModelDto update(String id, AiModelUpdateRequest request);

    List<AiModelDto> list(AiProviderType providerType);

    void delete(String id);
}
