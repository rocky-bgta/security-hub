package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.BreachSeverity;
import com.aspire.asat.phishing.dto.enums.BreachStatus;
import com.aspire.asat.phishing.model.BreachRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for breach records.
 */
@Repository
public interface BreachRecordRepository extends MongoRepository<BreachRecord, String> {

    /**
     * Find by client ID with pagination
     */
    Page<BreachRecord> findByClientIdOrderByCreatedAtDesc(String clientId, Pageable pageable);

    /**
     * Find by ID and client
     */
    Optional<BreachRecord> findByIdAndClientId(String id, String clientId);

    /**
     * Find by domain
     */
    Page<BreachRecord> findByClientIdAndDomainOrderByDateOfBreachDesc(
            String clientId, String domain, Pageable pageable);

    /**
     * Find by status
     */
    Page<BreachRecord> findByClientIdAndStatusOrderByCreatedAtDesc(
            String clientId, BreachStatus status, Pageable pageable);

    /**
     * Find by severity
     */
    Page<BreachRecord> findByClientIdAndSeverityOrderByCreatedAtDesc(
            String clientId, BreachSeverity severity, Pageable pageable);

    /**
     * Find breaches within date range
     */
    @Query("{ 'clientId': ?0, 'dateOfBreach': { $gte: ?1, $lte: ?2 } }")
    Page<BreachRecord> findByClientIdAndDateRange(
            String clientId, Instant startDate, Instant endDate, Pageable pageable);

    /**
     * Search by keyword (breach name or description)
     */
    @Query("{ 'clientId': ?0, $or: [ " +
           "{ 'breachName': { $regex: ?1, $options: 'i' } }, " +
           "{ 'description': { $regex: ?1, $options: 'i' } }, " +
           "{ 'domain': { $regex: ?1, $options: 'i' } } ] }")
    Page<BreachRecord> searchBreaches(String clientId, String keyword, Pageable pageable);

    /**
     * Find by external breach ID
     */
    Optional<BreachRecord> findByClientIdAndExternalBreachId(String clientId, String externalBreachId);

    /**
     * Count by client
     */
    long countByClientId(String clientId);

    /**
     * Count by status
     */
    long countByClientIdAndStatus(String clientId, BreachStatus status);

    /**
     * Count by severity
     */
    long countByClientIdAndSeverity(String clientId, BreachSeverity severity);

    /**
     * Find action required breaches
     */
    List<BreachRecord> findByClientIdAndStatusOrderByCreatedAtDesc(String clientId, BreachStatus status);

    /**
     * Find breaches by domain list
     */
    @Query("{ 'clientId': ?0, 'domain': { $in: ?1 } }")
    List<BreachRecord> findByClientIdAndDomainIn(String clientId, List<String> domains);
}
