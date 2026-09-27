package com.aspire.asat.registration.service.dropdown;

import com.aspire.asat.registration.data.dropdown.OrganizationSizeRequestDto;
import com.aspire.asat.registration.data.dropdown.OrganizationSizeRespDto;

import java.util.List;

public interface OrganizationSizeService {
    
    OrganizationSizeRespDto createOrganizationSize(OrganizationSizeRequestDto request);
    
    OrganizationSizeRespDto getOrganizationSizeById(String id);
    
    OrganizationSizeRespDto updateOrganizationSize(String id, OrganizationSizeRequestDto request);
    
    List<OrganizationSizeRespDto> getActiveOrganizationSizes();
    
    void deleteOrganizationSize(String id);
}