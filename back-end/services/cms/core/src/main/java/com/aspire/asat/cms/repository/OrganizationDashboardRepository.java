package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.OrganizationDashboard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Organization Dashboard (Super Admin Dashboard)
 * There is only ONE entry in the collection, identified by organizationAdminId from context
 */
@Repository
public interface OrganizationDashboardRepository extends MongoRepository<OrganizationDashboard, String> {
    
    /**
     * Find organization dashboard by organization admin ID
     * @param organizationAdminId the organization admin ID from user context
     * @return Optional OrganizationDashboard
     */
    Optional<OrganizationDashboard> findByOrganizationAdminId(String organizationAdminId);
    
    /**
     * Check if organization dashboard exists by organization admin ID
     * @param organizationAdminId the organization admin ID
     * @return true if exists, false otherwise
     */
    boolean existsByOrganizationAdminId(String organizationAdminId);
}

