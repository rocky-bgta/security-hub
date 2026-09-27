package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.SmsServerConfigurationRequest;
import com.aspire.asat.phishing.dto.request.SmsServerTestRequest;
import com.aspire.asat.phishing.dto.response.SmsServerConfigurationDto;
import com.aspire.asat.phishing.dto.response.TestResultDto;
import com.aspire.asat.phishing.model.SmsServerConfiguration;

import java.util.List;

public interface SmsServerConfigurationService {

    List<SmsServerConfigurationDto> getConfigurations(int offset, int pageSize, String clientId);

    long countConfigurations(String clientId);

    SmsServerConfigurationDto getById(String id);

    SmsServerConfigurationDto create(SmsServerConfigurationRequest request);

    SmsServerConfigurationDto update(String id, SmsServerConfigurationRequest request);

    void delete(String id);

    SmsServerConfigurationDto getDefault();

    SmsServerConfigurationDto setDefault(String id);

    TestResultDto testConfiguration(String id, SmsServerTestRequest request);

    SmsServerConfiguration resolveForCampaign(String clientId, String configurationId);
}
