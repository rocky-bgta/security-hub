package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.ClientProductReplica;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientProductReplicaRepository extends MongoRepository<ClientProductReplica, String> {
    
    /**
     * Find client product replicas by client admin ID
     * @param clientAdminId the client admin ID
     * @return List of ClientProductReplica
     */
    List<ClientProductReplica> findByClientAdminId(String clientAdminId);
    
    /**
     * Find client product replica by client admin ID, product ID, and package ID
     * @param clientAdminId the client admin ID
     * @param productId the product ID
     * @param packageId the package ID
     * @return Optional ClientProductReplica
     */
    Optional<ClientProductReplica> findByClientAdminIdAndProductIdAndPackageId(
            String clientAdminId, String productId, String packageId);
    
    /**
     * Delete all client product replicas by client admin ID
     * @param clientAdminId the client admin ID
     */
    void deleteByClientAdminId(String clientAdminId);
    
    /**
     * Find client product replicas by client admin ID and product ID
     * @param clientAdminId the client admin ID
     * @param productId the product ID
     * @return List of ClientProductReplica
     */
    List<ClientProductReplica> findByClientAdminIdAndProductId(String clientAdminId, String productId);

    /**
     * Find client product replicas for multiple client admin IDs
     * @param clientAdminIds list of client admin IDs
     * @return List of ClientProductReplica
     */
    List<ClientProductReplica> findByClientAdminIdIn(List<String> clientAdminIds);
}

