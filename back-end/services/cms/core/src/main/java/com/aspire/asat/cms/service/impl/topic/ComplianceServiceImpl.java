package com.aspire.asat.cms.service.impl.topic;

import com.aspire.asat.cms.dto.topic.ComplianceReqDto;
import com.aspire.asat.cms.dto.topic.ComplianceRespDto;
import com.aspire.asat.cms.exception.DuplicateDataFoundException;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.topic.Compliance;
import com.aspire.asat.cms.repository.topic.ComplianceRepository;
import com.aspire.asat.cms.service.topic.ComplianceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ComplianceServiceImpl implements ComplianceService {

    private final ComplianceRepository complianceRepository;

    @Override
    public ComplianceRespDto createCompliance(ComplianceReqDto complianceReqDto) {
        log.info("Creating compliance: {}", complianceReqDto);
        if(complianceRepository.existsByAcronymIgnoreCase(complianceReqDto.getAcronym())){
            throw new DuplicateDataFoundException("Compliance with acronym '" + complianceReqDto.getAcronym() + "' already exists.");
        }
        Compliance complianceToSave = Compliance.toCompliance(complianceReqDto);
        return Compliance.toComplianceRespDto(complianceRepository.save(complianceToSave));
    }

    @Override
    public ComplianceRespDto updateCompliance(String id, ComplianceReqDto complianceReqDto) {
        log.info("Updating compliance with id: {}", id);
        Compliance existingCompliance = complianceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compliance with id '" + id + "' not found."));

        if(!existingCompliance.getAcronym().equalsIgnoreCase(complianceReqDto.getAcronym())
                && complianceRepository.existsByAcronymIgnoreCase(complianceReqDto.getAcronym())){
            throw new DuplicateDataFoundException("Compliance with acronym '" + complianceReqDto.getAcronym() + "' already exists.");
        }
        updateComplianceData(existingCompliance, complianceReqDto);
        return Compliance.toComplianceRespDto(complianceRepository.save(existingCompliance));
    }


    @Override
    public ComplianceRespDto getById(String id) {
        log.info("Fetching compliance with id: {}", id);
        Compliance compliance = complianceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compliance with id '" + id + "' not found."));
        return Compliance.toComplianceRespDto(compliance);
    }

    @Override
    public void deleteComplianceById(String id) {
        log.info("Deleting compliance with id: {}", id);
        if(!complianceRepository.existsById(id)){
            throw new ResourceNotFoundException("Compliance with id '" + id + "' not found.");
        }
        complianceRepository.deleteById(id);
    }

    @Override
    public List<ComplianceRespDto> getAllCompliance() {
        log.info("Fetching all compliance records");
        Sort sort = Sort.by(Sort.Direction.ASC, "sortOrder");
        List<Compliance> compliances = complianceRepository.findAll(sort);
        return compliances.stream()
                .map(Compliance::toComplianceRespDto)
                .toList();
    }

    private void updateComplianceData(Compliance existingCompliance, ComplianceReqDto complianceReqDto) {

        if(complianceReqDto.getComplianceName() != null && !complianceReqDto.getComplianceName().isEmpty()){
            existingCompliance.setComplianceName(complianceReqDto.getComplianceName());
        }
        if(complianceReqDto.getAcronym() != null && !complianceReqDto.getAcronym().isEmpty()){
            existingCompliance.setAcronym(complianceReqDto.getAcronym());
        }
        if(complianceReqDto.getDescription() != null){
            existingCompliance.setDescription(complianceReqDto.getDescription());
        }
        if(complianceReqDto.getSortOrder() != null){
            existingCompliance.setSortOrder(complianceReqDto.getSortOrder());
        }
        existingCompliance.setUpdatedAt(java.time.Instant.now());

    }
}
