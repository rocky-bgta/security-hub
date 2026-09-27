package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.DomainStatus;
import com.aspire.asat.phishing.model.Domain;
import com.aspire.asat.phishing.repository.custom.DomainRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Domain entity operations.
 */
@Repository
public interface DomainRepository extends MongoRepository<Domain, String>, DomainRepositoryCustom {
    
    /**
     * Find all domains for a specific client
     */
    List<Domain> findByClientId(String clientId);
    
    /**
     * Find domains by client with pagination
     */
    Page<Domain> findByClientId(String clientId, Pageable pageable);

    /**
     * Find domains visible to the client (own + global)
     */
    @Query("{'$or': [{'clientId': ?0}, {'isGlobal': true}]}")
    Page<Domain> findByClientIdOrGlobal(String clientId, Pageable pageable);
    
    /**
     * Find a domain by name and client
     */
    Optional<Domain> findByDomainAndClientId(String domain, String clientId);
    
    /**
     * Find a domain by name only
     */
    Optional<Domain> findByDomain(String domain);
    
    /**
     * Find if a domain is locked by any tenant
     */
    Optional<Domain> findByDomainAndIsLockedTrue(String domain);
    
    /**
     * Check if domain exists and is locked by another tenant
     */
    boolean existsByDomainAndIsLockedTrueAndClientIdNot(String domain, String clientId);
    
    /**
     * Check if domain exists for a client
     */
    boolean existsByDomainAndClientId(String domain, String clientId);
    
    /**
     * Search domains by name pattern for a client
     */
    @Query("{'clientId': ?0, 'domain': {$regex: ?1, $options: 'i'}}")
    Page<Domain> searchByClientIdAndDomainContaining(String clientId, String searchPattern, Pageable pageable);

    /**
     * Search domains by name pattern for client-visible domains (own + global)
     */
    @Query("{'$and': [{'$or': [{'clientId': ?0}, {'isGlobal': true}]}, {'domain': {$regex: ?1, $options: 'i'}}]}")
    Page<Domain> searchByClientIdOrGlobalAndDomainContaining(String clientId, String searchPattern, Pageable pageable);
    
    /**
     * Count domains matching search for a client
     */
    @Query(value = "{'clientId': ?0, 'domain': {$regex: ?1, $options: 'i'}}", count = true)
    long countByClientIdAndDomainContaining(String clientId, String searchPattern);

    /**
     * Count domains visible to the client (own + global)
     */
    @Query(value = "{'$or': [{'clientId': ?0}, {'isGlobal': true}]}", count = true)
    long countByClientIdOrGlobal(String clientId);

    /**
     * Count domains matching search for client-visible domains (own + global)
     */
    @Query(value = "{'$and': [{'$or': [{'clientId': ?0}, {'isGlobal': true}]}, {'domain': {$regex: ?1, $options: 'i'}}]}", count = true)
    long countByClientIdOrGlobalAndDomainContaining(String clientId, String searchPattern);
    
    /**
     * Find domains by client and status
     */
    List<Domain> findByClientIdAndStatus(String clientId, DomainStatus status);

    /**
     * Find domain by id with visibility rule (own + global)
     */
    @Query("{'$and': [{'_id': ?0}, {'$or': [{'clientId': ?1}, {'isGlobal': true}]}]}")
    Optional<Domain> findByIdAndClientIdOrGlobal(String id, String clientId);
    
    /**
     * Find a domain by clientId and domain name
     * Alias for findByDomainAndClientId with swapped parameters
     */
    default Optional<Domain> findByClientIdAndDomainName(String clientId, String domainName) {
        return findByDomainAndClientId(domainName, clientId);
    }
}
