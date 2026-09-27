package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.RecipientBreachStatus;
import com.aspire.asat.phishing.model.RecipientBreach;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for recipient breaches.
 */
@Repository
public interface RecipientBreachRepository extends MongoRepository<RecipientBreach, String> {

    /**
     * Find by client with pagination
     */
    Page<RecipientBreach> findByClientIdOrderByCreatedAtDesc(String clientId, Pageable pageable);

    /**
     * Find by ID and client
     */
    Optional<RecipientBreach> findByIdAndClientId(String id, String clientId);

    /**
     * Find by breach record
     */
    Page<RecipientBreach> findByBreachRecordIdOrderByCreatedAtDesc(String breachRecordId, Pageable pageable);

    /**
     * Find by breach record (list)
     */
    List<RecipientBreach> findByBreachRecordId(String breachRecordId);

    /**
     * Find by user email
     */
    List<RecipientBreach> findByClientIdAndEmailOrderByCreatedAtDesc(String clientId, String email);

    /**
     * Find by user ID
     */
    List<RecipientBreach> findByClientIdAndUserIdOrderByCreatedAtDesc(String clientId, String userId);

    /**
     * Find by status
     */
    Page<RecipientBreach> findByClientIdAndStatusOrderByCreatedAtDesc(
            String clientId, RecipientBreachStatus status, Pageable pageable);

    /**
     * Search by email or name
     */
    @Query("{ 'clientId': ?0, $or: [ " +
           "{ 'email': { $regex: ?1, $options: 'i' } }, " +
           "{ 'firstName': { $regex: ?1, $options: 'i' } }, " +
           "{ 'lastName': { $regex: ?1, $options: 'i' } } ] }")
    Page<RecipientBreach> searchRecipients(String clientId, String keyword, Pageable pageable);

    /**
     * Check if recipient already tracked for breach
     */
    Optional<RecipientBreach> findByClientIdAndBreachRecordIdAndEmail(
            String clientId, String breachRecordId, String email);

    /**
     * Count by client
     */
    long countByClientId(String clientId);

    /**
     * Count by status
     */
    long countByClientIdAndStatus(String clientId, RecipientBreachStatus status);

    /**
     * Count by breach record
     */
    long countByBreachRecordId(String breachRecordId);

    /**
     * Find pending recipients
     */
    List<RecipientBreach> findByClientIdAndStatusOrderByCreatedAtDesc(
            String clientId, RecipientBreachStatus status);

    /**
     * Find recipients with multiple breaches
     */
    @Query("{ 'clientId': ?0, 'breachCount': { $gte: 2 } }")
    List<RecipientBreach> findMultiBreachRecipients(String clientId);

    /**
     * Delete by breach record
     */
    void deleteByBreachRecordId(String breachRecordId);
}
