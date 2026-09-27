package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.Policy;
import com.aspire.asat.universal.enums.PolicyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PolicyRepositoryCustom {

    /**
     * Find all policies with optional filters (policyName, countryId, industryId). Used for Super Admin, Aspire Admin, and Client Admin (all policies).
     */
    Page<Policy> findAllWithFilters(String policyName, String countryId, String industryId, Pageable pageable);

    Page<Policy> findByPolicyNameContainingIgnoreCase(String policyName, Pageable pageable);

    Page<Policy> findByStatusAndPolicyNameContainingIgnoreCase(PolicyStatus status, String policyName, Pageable pageable);

    Page<Policy> findByClientAdminId(String clientAdminId, Pageable pageable);

    Page<Policy> findByClientAdminIdAndPolicyNameContainingIgnoreCase(String clientAdminId, String policyName, Pageable pageable);

    /**
     * Find policies by clientAdminId with optional filters (policyName, countryId, industryId) for Client Admin list API.
     */
    Page<Policy> findByClientAdminIdWithFilters(String clientAdminId, String policyName, String countryId, String industryId, Pageable pageable);

    /**
     * Find policies by mspId with optional filters for MSP own-policy list API.
     */
    Page<Policy> findByMspIdWithFilters(String mspId, String policyName, String countryId, String industryId, Pageable pageable);

    /**
     * Active policies for client user: active policies created by their client admin OR default platform policies,
     * with optional filters (policyName, countryId, industryId).
     */
    Page<Policy> findActivePoliciesForClientUserWithFilters(String clientAdminId, String policyName, String countryId, String industryId, Pageable pageable);

    /**
     * Policies visible to a client user: default (platform) policies and policies for their client admin.
     */
    Page<Policy> findPoliciesForClientUserWithFilters(
            String clientAdminId,
            String policyName,
            String countryId,
            String industryId,
            Pageable pageable);

    /**
     * Policies visible to a client admin: default (platform) policies, policies for their MSP, and their own policies.
     */
    Page<Policy> findPoliciesForClientAdminWithFilters(
            String clientAdminId,
            String mspId,
            String policyName,
            String countryId,
            String industryId,
            Pageable pageable);

    /**
     * Policies visible to an MSP admin: default policies, own MSP policies, and policies for client admins under the MSP.
     */
    Page<Policy> findPoliciesForMspAdminWithFilters(
            String mspUserId,
            List<String> clientAdminIds,
            String policyName,
            String countryId,
            String industryId,
            Pageable pageable);
}
