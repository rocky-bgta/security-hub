package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.VishingScenarioCreateRequest;
import com.aspire.asat.phishing.dto.request.VishingScenarioUpdateRequest;
import com.aspire.asat.phishing.dto.response.VishingScenarioDto;

import java.util.List;

public interface VishingScenarioService {

    List<VishingScenarioDto> getScenarios(int offset, int pageSize, String searchParam);

    long countScenarios(String searchParam);

    VishingScenarioDto getById(String id);

    VishingScenarioDto create(VishingScenarioCreateRequest request);

    VishingScenarioDto update(String id, VishingScenarioUpdateRequest request);

    void delete(String id);

    VishingScenarioDto publish(String id);
}
