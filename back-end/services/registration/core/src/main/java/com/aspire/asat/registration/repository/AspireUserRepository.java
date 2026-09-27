package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.repository.custom.AspireUserRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AspireUserRepository extends MongoRepository<AspireUser, UUID>, AspireUserRepositoryCustom {

    Optional<AspireUser> findByEmailIgnoreCase(String email);
    
    Optional<AspireUser> findByUsername(String username);
    
    Optional<AspireUser> findByUserId(UUID baseUserId);

    /**
     * Find all AspireUsers by a list of userIds
     *
     * @param userIds The list of user IDs to find
     * @return List of AspireUsers matching the criteria
     */
    List<AspireUser> findByUserIdIn(List<UUID> userIds);
    
    boolean existsByEmail(String email);

    boolean existsByUserId(UUID baseUserId);
    
    List<AspireUser> findByUserType(String userType);
    List<AspireUser> findByUserTypeAndClientAdminId(String userType, String clientAdminId);

    List<AspireUser> findByStatus(String status);

    /**
     * Find all AspireUsers by clientAdminId and status
     *
     * @param clientAdminId The client admin ID
     * @param status        The status to filter by
     * @return List of AspireUsers matching the criteria
     */
    List<AspireUser> findByClientAdminIdAndStatus(String clientAdminId, String status);

    /**
     * Find all AspireUsers by clientAdminId and isAdminInactive flag
     *
     * @param clientAdminId   The client admin ID
     * @param isAdminInactive The isAdminInactive flag value
     * @return List of AspireUsers matching the criteria
     */
    List<AspireUser> findByClientAdminIdAndIsAdminInactive(String clientAdminId, Boolean isAdminInactive);

    /**
     * Find all AspireUsers by clientAdminId, status, and isAdminInactive flag
     *
     * @param clientAdminId   The client admin ID
     * @param status         The status to filter by
     * @param isAdminInactive The isAdminInactive flag value
     * @return List of AspireUsers matching the criteria
     */
    List<AspireUser> findByClientAdminIdAndStatusAndIsAdminInactive(String clientAdminId, String status, Boolean isAdminInactive);

    /**
     * Count AspireUsers under a client admin.
     */
    long countByClientAdminId(String clientAdminId);

    /**
     * Count AspireUsers under a client admin filtered by credential-sent flag.
     */
    long countByClientAdminIdAndIsCredentialSent(String clientAdminId, Boolean isCredentialSent);

    /**
     * Count AspireUsers under a client admin filtered by status.
     */
    long countByClientAdminIdAndStatus(String clientAdminId, String status);

    /**
     * Count AspireUsers by status
     *
     * @param status The status to filter by
     * @return Count of users with the specified status
     */
    long countByStatus(String status);

}

