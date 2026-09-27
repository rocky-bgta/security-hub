package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.VoiceServerConfigurationRequest;
import com.aspire.asat.phishing.dto.request.VoiceServerTestRequest;
import com.aspire.asat.phishing.dto.response.TestResultDto;
import com.aspire.asat.phishing.dto.response.VoiceServerConfigurationDto;
import com.aspire.asat.phishing.model.VoiceServerConfiguration;

import java.util.List;

public interface VoiceServerConfigurationService {

    List<VoiceServerConfigurationDto> getConfigurations(int offset, int pageSize, String clientId);

    long countConfigurations(String clientId);

    VoiceServerConfigurationDto getById(String id);

    VoiceServerConfigurationDto create(VoiceServerConfigurationRequest request);

    VoiceServerConfigurationDto update(String id, VoiceServerConfigurationRequest request);

    void delete(String id);

    VoiceServerConfigurationDto getDefault();

    VoiceServerConfigurationDto setDefault(String id);

    TestResultDto testConfiguration(String id, VoiceServerTestRequest request);

    VoiceServerConfiguration resolveForCampaign(String clientId, String configurationId);
}
