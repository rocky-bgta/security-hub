package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.UserLicence;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserLicenceRepository extends MongoRepository<UserLicence, String> {

    /**
     * Check if a licence already exists for a user and package combination
     * @param userId the user ID
     * @param packageId the package ID
     * @return true if licence exists, false otherwise
     */
    boolean existsByUserIdAndPackageId(String userId, String packageId);

    /**
     * Find licence by user ID and package ID
     * @param userId the user ID
     * @param packageId the package ID
     * @return Optional UserLicence
     */
    Optional<UserLicence> findByUserIdAndPackageId(String userId, String packageId);

    /**
     * Find all licences for a specific user
     * @param userId the user ID
     * @return list of UserLicence
     */
    List<UserLicence> findByUserId(String userId);

    /**
     * Find all licences for a specific client admin
     * @param clientAdminId the client admin ID
     * @return list of UserLicence
     */
    List<UserLicence> findByClientAdminId(String clientAdminId);

    /**
     * Find all licences for a specific package
     * @param packageId the package ID
     * @return list of UserLicence
     */
    List<UserLicence> findByPackageId(String packageId);

    /**
     * Count licences for a specific client admin and package combination
     * @param clientAdminId the client admin ID
     * @param packageId the package ID
     * @return count of licences
     */
    long countByClientAdminIdAndPackageId(String clientAdminId, String packageId);

    /**
     * Count all licences for a specific client admin
     * @param clientAdminId the client admin ID
     * @return count of licences
     */
    long countByClientAdminId(String clientAdminId);

    /**
     * Count licences for a specific client admin, product, and package combination
     * This ensures license limits are enforced per ClientProduct (product-package combination)
     * @param clientAdminId the client admin ID
     * @param productId the product ID
     * @param packageId the package ID
     * @return count of licences
     */
    long countByClientAdminIdAndProductIdAndPackageId(String clientAdminId, String productId, String packageId);

    /**
     * Seats that were already assigned as of {@code asOf}: ASSIGNED, issued on or before that instant,
     * and not expired at that instant.
     */
    @Query(value = "{ 'clientAdminId': ?0, 'licenceStatus': 'ASSIGNED', 'issueDate': { $lte: ?1 }, "
            + "$or: [ { 'expireDate': { $exists: false } }, { 'expireDate': null }, { 'expireDate': { $gt: ?1 } } ] }",
            count = true)
    long countAssignedAsOf(String clientAdminId, Instant asOf);
}
