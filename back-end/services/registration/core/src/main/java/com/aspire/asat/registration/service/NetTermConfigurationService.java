package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.netTerm.CreateNetTermConfigurationRequestDTO;
import com.aspire.asat.registration.data.netTerm.NetTermConfigurationDropdownDTO;
import com.aspire.asat.registration.data.netTerm.NetTermConfigurationResponseDTO;
import com.aspire.asat.registration.data.netTerm.UpdateNetTermConfigurationRequestDTO;

import java.util.List;

public interface NetTermConfigurationService {

    NetTermConfigurationResponseDTO createNetTerm(CreateNetTermConfigurationRequestDTO dto);

    List<NetTermConfigurationResponseDTO> getAllNetTerms(String search, Boolean isActive, int offset, int limit);

    long getTotalNetTermCount(String search, Boolean isActive);

    NetTermConfigurationResponseDTO getNetTermById(String id);

    NetTermConfigurationResponseDTO updateNetTerm(String id, UpdateNetTermConfigurationRequestDTO dto);

    void deleteNetTerm(String id);

    List<NetTermConfigurationDropdownDTO> getAllActiveNetTerms();
}

