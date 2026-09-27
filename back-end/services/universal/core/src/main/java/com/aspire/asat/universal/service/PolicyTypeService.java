package com.aspire.asat.universal.service;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.universal.entity.PolicyType;
import com.aspire.asat.universal.enums.Status;
import com.aspire.asat.universal.policy.PolicyTypeDto;
import com.aspire.asat.universal.policy.PolicyTypeRequest;
import com.aspire.asat.universal.repository.PolicyTypeRepository;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import com.aspire.asat.universal.data.apiresponse.PaginatedResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PolicyTypeService {

    private final PolicyTypeRepository policyTypeRepository;
    private final UserCurrentContextService userCurrentContextService;
    private final ModelMapper modelMapper;


    public PaginatedResponseDto<PolicyTypeDto> getAllPolicyTypes(int limit, int offset) {
        Pageable pageable = PageRequest.of(offset, limit);
        Page<PolicyType> policyTypePage = policyTypeRepository.findAll(pageable);

        List<PolicyTypeDto> policyTypeDtos = policyTypePage.getContent().stream()
                .map(policyType -> modelMapper.map(policyType, PolicyTypeDto.class))
                .toList();

        return new PaginatedResponseDto<>(
                policyTypeDtos,
                policyTypePage.getTotalElements(),
                limit,
                offset
        );
    }

    public List<PolicyTypeDto> getActivePolicyTypes() {
        return policyTypeRepository.findByStatus(Status.ACTIVE).stream()
                .map(policyType -> modelMapper.map(policyType, PolicyTypeDto.class))
                .toList();
    }

    public PolicyTypeDto getPolicyTypeById(String id) {
        Optional<PolicyType> policyType = policyTypeRepository.findById(id);
        return policyType.map(pt -> modelMapper.map(pt, PolicyTypeDto.class)).orElse(null);
    }

    public PolicyTypeDto createPolicyType(PolicyTypeRequest policyTypeRequest) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();

        PolicyType policyType = modelMapper.map(policyTypeRequest, PolicyType.class);
        policyType.setId(UUID.randomUUID().toString());
        policyType.setCreatedBy(context.getUserId());
        policyType.setCreatedAt(LocalDateTime.now());
        policyType.setUpdatedBy(context.getUserId());
        policyType.setUpdatedAt(LocalDateTime.now());

        PolicyType savedPolicyType = policyTypeRepository.save(policyType);
        return modelMapper.map(savedPolicyType, PolicyTypeDto.class);
    }

    public PolicyTypeDto updatePolicyType(String id, PolicyTypeRequest policyTypeRequest) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();

        Optional<PolicyType> existingPolicyTypeOptional = policyTypeRepository.findById(id);
        if (existingPolicyTypeOptional.isPresent()) {
            PolicyType existingPolicyType = existingPolicyTypeOptional.get();
            modelMapper.map(policyTypeRequest, existingPolicyType);
            existingPolicyType.setId(id);
            existingPolicyType.setUpdatedBy(context.getUserId());
            existingPolicyType.setUpdatedAt(LocalDateTime.now());

            PolicyType updatedPolicyType = policyTypeRepository.save(existingPolicyType);
            return modelMapper.map(updatedPolicyType, PolicyTypeDto.class);
        }
        return null;
    }

    public boolean deletePolicyType(String id) {
        if (policyTypeRepository.existsById(id)) {
            policyTypeRepository.deleteById(id);
            return true;
        }
        return false;
    }
}

