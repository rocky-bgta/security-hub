package com.aspire.asat.cms.service.topic;


import com.aspire.asat.cms.dto.topic.ComplianceReqDto;
import com.aspire.asat.cms.dto.topic.ComplianceRespDto;

import java.util.List;

public interface ComplianceService {
    ComplianceRespDto createCompliance(ComplianceReqDto complianceReqDto);
    ComplianceRespDto updateCompliance(String id, ComplianceReqDto complianceReqDto);
    ComplianceRespDto getById(String id);
    void deleteComplianceById(String id);
    List<ComplianceRespDto> getAllCompliance();
}
