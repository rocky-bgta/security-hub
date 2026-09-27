package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.custom.UserSubPackageRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubPackageRepository extends MongoRepository<UserSubPackage, String>, UserSubPackageRepositoryCustom {

    List<UserSubPackage> findByUserId(String userId);

    Optional<UserSubPackage> findByUserIdAndSubPackageId(String userId, String subPackageId);

    boolean existsByUserIdAndSubPackageId(String userId, String subPackageId);

    void deleteByUserIdAndSubPackageId(String userId, String subPackageId);

    List<UserSubPackage> findByUserIdAndStatus(String userId, String status);

    // For a single subpackage (no pagination)
    List<UserSubPackage> findByUserIdAndSubPackageIdAndStatusAndCertificateLinkIsNotNull(String userId, String subPackageId, String status);

    // For all subpackages (with pagination)
    List<UserSubPackage> findByUserIdAndStatusAndCertificateLinkIsNotNull(String userId, String status, Pageable pageable);

    // Count users assigned to a specific subpackage
    long countBySubPackageId(String subPackageId);

    /**
     * Paginated assigned users for a sub-package, ordered by assignedDate descending.
     */
    Page<UserSubPackage> findBySubPackageId(String subPackageId, Pageable pageable);

    // Count users assigned to multiple subpackages (for batch operations)
    List<UserSubPackage> findBySubPackageIdIn(List<String> subPackageIds);

    // Find all user sub-packages by client admin ID (for risk analysis)
    List<UserSubPackage> findByClientAdminId(String clientAdminId);

    /**
     * Count user sub-packages for a client admin filtered by status (e.g. COMPLETED).
     */
    long countByClientAdminIdAndStatus(String clientAdminId, String status);

    // Find user sub-packages by user ID and client admin ID (for risk analysis)
    List<UserSubPackage> findByUserIdAndClientAdminId(String userId, String clientAdminId);

    /**
     * Find non-phishing user sub-packages by user ID and client admin ID (for training risk analysis).
     * Includes documents where isPhishingSubpackage is false, null, or missing.
     */
    @Query("{ 'userId': ?0, 'clientAdminId': ?1, 'isPhishingSubpackage': { $ne: true } }")
    List<UserSubPackage> findByUserIdAndClientAdminIdAndIsPhishingSubpackageNotTrue(String userId, String clientAdminId);

    // Find all user sub-packages by client admin ID and product ID (for risk analysis by product)
    List<UserSubPackage> findByClientAdminIdAndProductId(String clientAdminId, String productId);

    // Find user sub-packages by user ID, client admin ID and product ID (for risk analysis by product)
    List<UserSubPackage> findByUserIdAndClientAdminIdAndProductId(String userId, String clientAdminId, String productId);

    // ========== NEW METHODS FOR OPTIMIZED PAGINATION ==========
    
    /**
     * Find user subpackages with database-level pagination and sorting
     * @param userId the user ID
     * @param pageable pagination and sorting information
     * @return page of user subpackages
     */
    Page<UserSubPackage> findByUserId(String userId, Pageable pageable);
    
    /**
     * Count user subpackages efficiently without loading all records
     * @param userId the user ID
     * @return count of user subpackages
     */
    long countByUserId(String userId);
    
    /**
     * Find user subpackages by user ID ordered by assignment date ascending
     * This ensures consistent ordering based on when subpackages were assigned to the user
     * @param userId the user ID
     * @return list of user subpackages ordered by assignment date
     */
    List<UserSubPackage> findByUserIdOrderByAssignedDateAsc(String userId);
    
    /**
     * Find user subpackages by user ID and multiple subpackage IDs
     * This eliminates the need to load all user subpackages and filter in memory
     * @param userId the user ID
     * @param subPackageIds list of subpackage IDs
     * @return list of user subpackages matching the criteria
     */
    List<UserSubPackage> findByUserIdAndSubPackageIdIn(String userId, List<String> subPackageIds);

    // ========== METHODS FOR EXPIRY REMINDER SCHEDULER ==========
    
    /**
     * Find all active user subpackages that are not completed and not expired
     * Used by the expiry reminder scheduler to find subpackages that may need reminders
     * @param excludeStatuses list of statuses to exclude (e.g., "COMPLETED")
     * @param expiryDateFrom minimum expiry date (typically today or future)
     * @return list of active user subpackages
     */
    @Query("{ 'status': { $nin: ?0 }, 'expiryDate': { $gte: ?1 } }")
    List<UserSubPackage> findActiveSubPackagesForReminders(List<String> excludeStatuses, LocalDate expiryDateFrom);

}
