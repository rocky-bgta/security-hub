package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.ClientDashboard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientDashboardRepository extends MongoRepository<ClientDashboard, String> {
    
    /**
     * Find client dashboard by client admin ID
     * @param clientAdminId the client admin ID
     * @return Optional ClientDashboard
     */
    Optional<ClientDashboard> findByClientAdminId(String clientAdminId);
    
    /**
     * Check if client dashboard exists by client admin ID
     * @param clientAdminId the client admin ID
     * @return true if exists, false otherwise
     */
    boolean existsByClientAdminId(String clientAdminId);
}
