package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.AttackTechniqueCreateRequest;
import com.aspire.asat.phishing.dto.request.AttackTechniqueUpdateRequest;
import com.aspire.asat.phishing.dto.response.AttackTechniqueDto;

import java.util.List;

/**
 * Service for configurable attack technique catalog (Mongo).
 */
public interface AttackTechniqueService {

    AttackTechniqueDto createAttackTechnique(AttackTechniqueCreateRequest request);

    AttackTechniqueDto updateAttackTechnique(String id, AttackTechniqueUpdateRequest request);

    void deleteAttackTechnique(String id);

    AttackTechniqueDto getAttackTechniqueById(String id);

    List<AttackTechniqueDto> getAttackTechniques(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    long countAttackTechniques(String searchParam, boolean isActive);
}
