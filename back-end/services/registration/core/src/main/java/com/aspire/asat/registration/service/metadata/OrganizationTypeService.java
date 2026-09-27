package com.aspire.asat.registration.service.metadata;

import com.aspire.asat.registration.data.metadata.request.OrganizationTypeRequestDTO;
import com.aspire.asat.registration.data.metadata.request.OrganizationTypeUpdateRequestDTO;
import com.aspire.asat.registration.data.metadata.response.OrganizationTypeList;
import com.aspire.asat.registration.data.metadata.response.OrganizationTypeResponseDTO;

import java.util.List;

public interface OrganizationTypeService {

    OrganizationTypeResponseDTO createOrganizationType(OrganizationTypeRequestDTO requestDTO);
    
    OrganizationTypeResponseDTO getOrganizationTypeById(String id);
    
    List<OrganizationTypeList> getAllOrganizationTypes();
    
    OrganizationTypeResponseDTO updateOrganizationType(OrganizationTypeUpdateRequestDTO requestDTO);
    
    void deleteOrganizationType(String id);
}
