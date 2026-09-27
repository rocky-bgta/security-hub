package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.AttackerPersonaCreateRequest;
import com.aspire.asat.phishing.dto.request.AttackerPersonaUpdateRequest;
import com.aspire.asat.phishing.dto.response.AttackerPersonaDto;

import java.util.List;

/**
 * Service for configurable attacker persona catalog (Mongo).
 */
public interface AttackerPersonaService {

    AttackerPersonaDto createAttackerPersona(AttackerPersonaCreateRequest request);

    AttackerPersonaDto updateAttackerPersona(String id, AttackerPersonaUpdateRequest request);

    void deleteAttackerPersona(String id);

    AttackerPersonaDto getAttackerPersonaById(String id);

    List<AttackerPersonaDto> getAttackerPersonas(String searchParam, boolean isActive, int offset, int pageSize,
                                               String sortBy, String sortOrder);

    long countAttackerPersonas(String searchParam, boolean isActive);
}
