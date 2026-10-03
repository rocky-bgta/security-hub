package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.EndUserPackage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EndUserPackageRepository extends MongoRepository<EndUserPackage, String> {

    Optional<EndUserPackage> findByUserIdAndProductId(String endUserId, String productId);
    
    List<EndUserPackage> findBySubPackageId(String subPackageId);

    boolean existsByUserIdAndSubPackageId(String endUserId, String productPackageId);

    /**
     * Find EndUserPackage by userId and subPackageId
     * @param endUserId the user ID
     * @param subPackageId the subpackage ID
     * @return Optional EndUserPackage
     */
    Optional<EndUserPackage> findByUserIdAndSubPackageId(String endUserId, String subPackageId);

    /**
     * Find all active sub-packages assigned to a specific user
     * @param userId the user ID
     * @return list of active sub-packages assigned to the user
     */
    List<EndUserPackage> findByUserIdAndActiveTrue(String userId);
    List<EndUserPackage> findAllByClientAdminIdAndActiveTrue(String clientAdminId);

    /**
     * Count all end-user packages for a client admin (training assigned).
     */
    long countByClientAdminId(String clientAdminId);

    long countByStatus(String status);

    /**
     * Find all active sub-packages assigned to a specific user with a specific status
     * @param userId the user ID
     * @param status the package status (NOT_STARTED, IN_PROGRESS, COMPLETED)
     * @return list of active sub-packages assigned to the user with the specified status
     */
    List<EndUserPackage> findByUserIdAndStatusAndActiveTrue(String userId, String status);

}
