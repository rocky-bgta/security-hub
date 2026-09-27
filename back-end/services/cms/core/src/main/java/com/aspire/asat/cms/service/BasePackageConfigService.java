package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.basePackage.BasePackageConfigRequest;
import com.aspire.asat.cms.dto.basePackage.BasePackageConfigResponse;
import com.aspire.asat.cms.dto.basePackage.BasePackageConfigUpdateRequest;

import java.util.List;

public interface BasePackageConfigService {
    
    BasePackageConfigResponse createBasePackageConfig(BasePackageConfigRequest request);
    
    List<BasePackageConfigResponse> getAllBasePackageConfigs();
    
    BasePackageConfigResponse getBasePackageConfigById(String basePackageId);
    
    BasePackageConfigResponse updateBasePackageConfig(String basePackageId, BasePackageConfigUpdateRequest request);
    
    String deleteBasePackageConfig(String basePackageId);
    
    boolean existsByName(String name);
}
