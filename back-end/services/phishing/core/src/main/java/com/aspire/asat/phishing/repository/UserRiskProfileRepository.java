package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.repository.custom.UserRiskProfileRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for user risk profiles.
 */
@Repository
public interface UserRiskProfileRepository extends MongoRepository<UserRiskProfile, String>, UserRiskProfileRepositoryCustom {

    /**
     * Find by client and user
     */
    Optional<UserRiskProfile> findByUserId(String userId);

    /**
     * Find by client id and user id (unique per client)
     */
    Optional<UserRiskProfile> findByClientIdAndUserId(String clientId, String userId);

    /**
     * Find all profiles for a client and given user ids (for batch existence check).
     */
    List<UserRiskProfile> findByClientIdAndUserIdIn(String clientId, List<String> userIds);

    /**
     * Find by client and email
     */
    Optional<UserRiskProfile> findByClientIdAndEmail(String clientId, String email);

    /**
     * Batch lookup profiles by email for dashboard top-risk user enrichment.
     */
    List<UserRiskProfile> findByClientIdAndEmailIn(String clientId, Collection<String> emails);

    /**
     * Find all profiles for client
     */
    List<UserRiskProfile> findByClientId(String clientId);

    /**
     * Find all profiles for client
     */
    Page<UserRiskProfile> findByClientIdOrderByRiskScoreDesc(String clientId, Pageable pageable);

    /**
     * Find by risk level
     */
    Page<UserRiskProfile> findByClientIdAndRiskLevelOrderByRiskScoreDesc(
            String clientId, RiskLevel riskLevel, Pageable pageable);

    /**
     * Find high and critical risk users
     */
    @Query("{ 'clientId': ?0, 'riskLevel': { $in: ['HIGH', 'CRITICAL'] } }")
    List<UserRiskProfile> findHighRiskUsers(String clientId);

    /**
     * Find repeat offenders (clicked 3+ times)
     */
    @Query("{ 'clientId': ?0, 'linksClicked': { $gte: 3 } }")
    List<UserRiskProfile> findRepeatOffenders(String clientId);

    /**
     * Count by risk level
     */
    long countByClientIdAndRiskLevel(String clientId, RiskLevel riskLevel);

    /**
     * Count total users
     */
    long countByClientId(String clientId);

    /**
     * Count repeat offenders
     */
    @Query(value = "{ 'clientId': ?0, 'linksClicked': { $gte: 3 } }", count = true)
    long countRepeatOffenders(String clientId);

    /**
     * Count compromised users (submitted data)
     */
    @Query(value = "{ 'clientId': ?0, 'dataSubmissions': { $gte: 1 } }", count = true)
    long countCompromisedUsers(String clientId);

    /**
     * Find top risky users
     */
    List<UserRiskProfile> findTop10ByClientIdOrderByRiskScoreDesc(String clientId);

    /**
     * Find top phishing risky users
     */
    List<UserRiskProfile> findTop10ByClientIdOrderByPhishingRiskScoreDesc(String clientId);
}
