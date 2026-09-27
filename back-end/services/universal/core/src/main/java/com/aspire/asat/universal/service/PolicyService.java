package com.aspire.asat.universal.service;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.universal.data.externalresponses.ComplianceDto;
import com.aspire.asat.universal.data.externalresponses.CountryDto;
import com.aspire.asat.universal.data.externalresponses.IndustryDto;
import com.aspire.asat.universal.entity.Policy;
import com.aspire.asat.universal.entity.PolicyType;
import com.aspire.asat.universal.enums.PolicyStatus;
import com.aspire.asat.universal.policy.PolicyDto;
import com.aspire.asat.universal.policy.PolicyRequest;
import com.aspire.asat.universal.repository.PolicyRepository;
import com.aspire.asat.universal.repository.PolicyTypeRepository;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import com.aspire.asat.universal.utils.UserTypeUtils;
import com.aspire.asat.universal.data.apiresponse.PaginatedResponseDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyTypeRepository policyTypeRepository;
    private final UserCurrentContextService userCurrentContextService;
    private final ModelMapper modelMapper;
    private final ExternalApiService externalApiService;


    public PaginatedResponseDto<PolicyDto> getAllPolicies(int pageSize, int offset) {
        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<Policy> policyPage = policyRepository.findAll(pageable);

        List<PolicyDto> policyDtos = policyPage.getContent().stream()
                .map(this::convertToDto)
                .toList();

        return new PaginatedResponseDto<>(
                policyDtos,
                policyPage.getTotalElements(),
                pageSize,
                offset
        );
    }

    // New overload: preserve existing behavior if policyName is null/blank
    public PaginatedResponseDto<PolicyDto> getAllPolicies(int pageSize, int offset, String policyName) {
        if (policyName == null || policyName.isBlank()) {
            return getAllPolicies(pageSize, offset);
        }
        if (offset < 0) offset = 0;
        if (pageSize <= 0) pageSize = 10;
        // Treat offset as page number (0-based)
        Pageable pageable = PageRequest.of(offset, pageSize);

        Page<Policy> policyPage = policyRepository.findByPolicyNameContainingIgnoreCase(policyName, pageable);

        List<PolicyDto> policyDtos = policyPage.getContent().stream()
                .map(this::convertToDto)
                .toList();

        return new PaginatedResponseDto<>(
                policyDtos,
                policyPage.getTotalElements(),
                pageSize,
                offset
        );
    }

    /**
     * Returns policies based on current user context:
     * - Aspire Admin / Super Admin / System User: all policies
     * - Client Admin: if isOwnPolicy=true then only their own policies; otherwise default + MSP + own policies
     * - MSP Admin: if isOwnPolicy=true then only their MSP policies; otherwise default + own + client admin policies
     * - Client User: if isOwnPolicy=true then only their client admin policies; otherwise default (platform) policies and client admin policies
     */
    public PaginatedResponseDto<PolicyDto> getPoliciesForCurrentUser(int pageSize, int offset, String policyName,
                                                                     String countryId, String industryId, Boolean isOwnPolicy) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        String userType = context.getUserType() != null ? context.getUserType() : "";

        if (offset < 0) offset = 0;
        if (pageSize <= 0) pageSize = 10;
        Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Policy> policyPage;
        if (isPlatformAdmin(userType)) {
            policyPage = policyRepository.findAllWithFilters(policyName, countryId, industryId, pageable);
        } else if (UserType.CLIENT_ADMIN.getValue().equalsIgnoreCase(userType)) {
            if (Boolean.TRUE.equals(isOwnPolicy)) {
                policyPage = policyRepository.findByClientAdminIdWithFilters(
                        context.getUserId(), policyName, countryId, industryId, pageable);
            } else {
                policyPage = policyRepository.findPoliciesForClientAdminWithFilters(
                        context.getUserId(),
                        context.getMspId(),
                        policyName,
                        countryId,
                        industryId,
                        pageable);
            }
        } else if (UserType.MSP.getValue().equalsIgnoreCase(userType)) {
            if (Boolean.TRUE.equals(isOwnPolicy)) {
                policyPage = policyRepository.findByMspIdWithFilters(
                        context.getUserId(), policyName, countryId, industryId, pageable);
            } else {
                policyPage = policyRepository.findPoliciesForMspAdminWithFilters(
                        context.getUserId(),
                        resolveClientAdminIds(context),
                        policyName,
                        countryId,
                        industryId,
                        pageable);
            }
        } else if (UserType.USER.getValue().equalsIgnoreCase(userType)) {
            String clientAdminId = context.getClientAdminId();
            if (Boolean.TRUE.equals(isOwnPolicy)) {
                policyPage = policyRepository.findByClientAdminIdWithFilters(
                        clientAdminId, policyName, countryId, industryId, pageable);
            } else {
                policyPage = policyRepository.findPoliciesForClientUserWithFilters(
                        clientAdminId, policyName, countryId, industryId, pageable);
            }
        } else {
            policyPage = policyRepository.findAllWithFilters(policyName, countryId, industryId, pageable);
        }

        List<PolicyDto> policyDtos = policyPage.getContent().stream()
                .map(policy -> convertToDto(policy, context))
                .toList();

        return new PaginatedResponseDto<>(
                policyDtos,
                policyPage.getTotalElements(),
                pageSize,
                offset
        );
    }

    public PaginatedResponseDto<PolicyDto> getActivePolicies(int pageSize, int offset) {
        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<Policy> policyPage = policyRepository.findByStatus(PolicyStatus.ACTIVE, pageable);

        List<PolicyDto> policyDtos = policyPage.getContent().stream()
                .map(this::convertToDto)
                .toList();

        return new PaginatedResponseDto<>(
                policyDtos,
                policyPage.getTotalElements(),
                pageSize,
                offset
        );
    }

    // New overload for active policies with name filter
    public PaginatedResponseDto<PolicyDto> getActivePolicies(int pageSize, int offset, String policyName) {
        if (policyName == null || policyName.isBlank()) {
            return getActivePolicies(pageSize, offset);
        }
        if (offset < 0) offset = 0;
        if (pageSize <= 0) pageSize = 10;
        // Treat offset as page number (0-based)
        Pageable pageable = PageRequest.of(offset, pageSize);
        Page<Policy> policyPage = policyRepository.findByStatusAndPolicyNameContainingIgnoreCase(PolicyStatus.ACTIVE, policyName, pageable);

        List<PolicyDto> policyDtos = policyPage.getContent().stream()
                .map(this::convertToDto)
                .toList();

        return new PaginatedResponseDto<>(
                policyDtos,
                policyPage.getTotalElements(),
                pageSize,
                offset
        );
    }

    public PolicyDto getPolicyById(String id) {
        Optional<Policy> policyOptional = policyRepository.findById(id);

        if (policyOptional.isEmpty()) {
            return null;
        }

        Policy policy = policyOptional.get();
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        assertMspCanViewPolicy(policy, context);
        PolicyDto policyDto = convertToDto(policy, context);

        // Make parallel external API calls to enrich the policy data
        Mono< Optional<CountryDto>> countryMono = externalApiService.getCountryById(policy.getCountryId())
                .map(Optional::of)
                .defaultIfEmpty( Optional.empty());
        Mono< Optional<IndustryDto>> industryMono = externalApiService.getIndustryById(policy.getIndustryId())
                .map( Optional::of)
                .defaultIfEmpty( Optional.empty());
        Mono< Optional<ComplianceDto>> complianceMono = externalApiService.getComplianceById(policy.getComplianceId())
                .map( Optional::of)
                .defaultIfEmpty( Optional.empty());

        // Block to wait for all external calls to complete and process results
        Mono.zip(countryMono, industryMono, complianceMono)
                .doOnNext(tuple -> {
                     Optional<CountryDto> countryOpt = tuple.getT1();
                     Optional<IndustryDto> industryOpt = tuple.getT2();
                     Optional<ComplianceDto> complianceOpt = tuple.getT3();

                    // Map country data
                    countryOpt.ifPresent(countryDto -> {
                        PolicyDto.CountryInfo countryInfo = new PolicyDto.CountryInfo();
                        countryInfo.setId(countryDto.getId());
                        countryInfo.setCode(countryDto.getCode());
                        countryInfo.setName(countryDto.getName());
                        policyDto.setCountry(countryInfo);
                    });

                    // Map industry data
                    industryOpt.ifPresent(industryDto -> {
                        PolicyDto.IndustryInfo industryInfo = new PolicyDto.IndustryInfo();
                        industryInfo.setId(industryDto.getId());
                        industryInfo.setCode(industryDto.getCode());
                        industryInfo.setName(industryDto.getName());
                        policyDto.setIndustry(industryInfo);
                    });

                    // Map compliance data
                    complianceOpt.ifPresent(complianceDto -> {
                        PolicyDto.ComplianceInfo complianceInfo = new PolicyDto.ComplianceInfo();
                        complianceInfo.setId(complianceDto.getId());
                        complianceInfo.setComplianceName(complianceDto.getComplianceName());
                        complianceInfo.setDescription(complianceDto.getDescription());
                        policyDto.setCompliance(complianceInfo);
                    });
                })
                .block();
        return policyDto;
    }

    public PolicyDto createPolicy(PolicyRequest policyRequest) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();

        Policy policy = modelMapper.map(policyRequest, Policy.class);
        policy.setId(UUID.randomUUID().toString());
        policy.setCreatedBy(context.getUserId());
        policy.setCreatedAt(LocalDateTime.now());
        policy.setUpdatedBy(context.getUserId());
        policy.setUpdatedAt(LocalDateTime.now());

        if (isPlatformAdmin(context.getUserType())) {
            policy.setIsDefault(true);
            policy.setClientAdminId(null);
            policy.setMspId(null);
        } else {
            policy.setIsDefault(false);
            if (UserType.CLIENT_ADMIN.getValue().equalsIgnoreCase(context.getUserType())) {
                policy.setClientAdminId(context.getUserId());
            } else if (UserType.MSP.getValue().equalsIgnoreCase(context.getUserType())) {
                policy.setMspId(context.getUserId());
            }
        }

        Policy savedPolicy = policyRepository.save(policy);
        return convertToDto(savedPolicy, context);
    }

    public PolicyDto updatePolicy(String id, PolicyRequest policyRequest) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();

        Optional<Policy> existingPolicyOptional = policyRepository.findById(id);
        if (existingPolicyOptional.isPresent()) {
            Policy existingPolicy = existingPolicyOptional.get();
            assertMspCanModifyPolicy(existingPolicy, context);

            modelMapper.map(policyRequest, existingPolicy);
            existingPolicy.setId(id);
            if (isPlatformAdmin(context.getUserType())) {
                existingPolicy.setIsDefault(true);
            }
            existingPolicy.setUpdatedBy(context.getUserId());
            existingPolicy.setUpdatedAt(LocalDateTime.now());

            Policy updatedPolicy = policyRepository.save(existingPolicy);
            return convertToDto(updatedPolicy, context);
        }
        return null;
    }

    public boolean deletePolicy(String id) {
        CurrentUserContext context = userCurrentContextService.getCurrentUserContext();
        Optional<Policy> policyOptional = policyRepository.findById(id);
        if (policyOptional.isEmpty()) {
            return false;
        }
        assertMspCanModifyPolicy(policyOptional.get(), context);
        policyRepository.deleteById(id);
        return true;
    }

    private void assertMspCanModifyPolicy(Policy policy, CurrentUserContext context) {
        if (!UserTypeUtils.isMspAdmin(context.getUserType())) {
            return;
        }
        if (!isMspOwnedPolicy(policy, context.getUserId())) {
            throw new IllegalArgumentException("You can only modify policies created by you");
        }
    }

    private void assertMspCanViewPolicy(Policy policy, CurrentUserContext context) {
        if (!UserTypeUtils.isMspAdmin(context.getUserType())) {
            return;
        }
        String mspUserId = context.getUserId();
        if (isMspOwnedPolicy(policy, mspUserId)) {
            return;
        }
        if (Boolean.TRUE.equals(policy.getIsDefault())) {
            return;
        }
        if (policy.getClientAdminId() != null
                && resolveClientAdminIds(context).contains(policy.getClientAdminId())) {
            return;
        }
        throw new IllegalArgumentException("Policy not found");
    }

    private boolean isPlatformAdmin(String userType) {
        if (userType == null || userType.isBlank()) {
            return false;
        }
        return UserType.SYSTEM_USER.getValue().equalsIgnoreCase(userType)
                || UserType.SUPER_ADMIN.getValue().equalsIgnoreCase(userType)
                || UserType.ASPIRE_ADMIN.getValue().equalsIgnoreCase(userType);
    }

    private boolean isMspOwnedPolicy(Policy policy, String mspUserId) {
        return mspUserId != null
                && mspUserId.equals(policy.getCreatedBy())
                && mspUserId.equals(policy.getMspId());
    }

    private List<String> resolveClientAdminIds(CurrentUserContext context) {
        List<String> clientAdminIds = context.getClientAdminIds();
        if (clientAdminIds == null || clientAdminIds.isEmpty()) {
            return List.of();
        }
        return clientAdminIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .toList();
    }

    private PolicyDto convertToDto(Policy policy) {
        return convertToDto(policy, null);
    }

    private PolicyDto convertToDto(Policy policy, CurrentUserContext context) {
        PolicyDto policyDto = modelMapper.map(policy, PolicyDto.class);

        if (context != null && UserTypeUtils.isMspAdmin(context.getUserType())) {
            policyDto.setEditable(isMspOwnedPolicy(policy, context.getUserId()));
        }

        // Fetch policy type name if policy type ID exists
        if (policy.getPolicyTypeId() != null) {
            Optional<PolicyType> policyType = policyTypeRepository.findById(policy.getPolicyTypeId());
            policyType.ifPresent(type -> policyDto.setPolicyTypeName(type.getName()));
        }

        return policyDto;
    }
}
