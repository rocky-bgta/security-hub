package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.LandingPageType;
import com.aspire.asat.phishing.model.LandingPage;
import com.aspire.asat.phishing.repository.custom.LandingPageRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for LandingPage entity operations.
 */
@Repository
public interface LandingPageRepository extends MongoRepository<LandingPage, String>, LandingPageRepositoryCustom {

    @Query("{'$or': [{'clientId': ?0}, {'isGlobal': true}]}")
    Page<LandingPage> findByClientIdOrGlobal(String clientId, Pageable pageable);

    @Query("{'$and': [" +
           "{'$or': [{'clientId': ?0}, {'isGlobal': true}]}," +
           "{'$or': [" +
           "  {'name': {'$regex': ?1, '$options': 'i'}}," +
           "  {'tags': {'$regex': ?1, '$options': 'i'}}" +
           "]}" +
           "]}")
    Page<LandingPage> searchByClientIdAndKeyword(String clientId, String keyword, Pageable pageable);

    @Query("{'$and': [{'_id': ?0}, {'$or': [{'clientId': ?1}, {'isGlobal': true}]}]}")
    Optional<LandingPage> findByIdAndClientIdOrGlobal(String id, String clientId);

    @Query(value = "{'$and': [" +
           "{'$or': [{'clientId': ?0}, {'isGlobal': true}]}," +
           "{'$or': [" +
           "  {'name': {'$regex': ?1, '$options': 'i'}}," +
           "  {'tags': {'$regex': ?1, '$options': 'i'}}" +
           "]}" +
           "]}", count = true)
    long countByClientIdAndKeyword(String clientId, String keyword);

    @Query("{'$and': [{'$or': [{'clientId': ?0}, {'isGlobal': true}]}, {'pageType': ?1}]}")
    Page<LandingPage> findByClientIdAndPageType(String clientId, LandingPageType pageType, Pageable pageable);

    @Query(value = "{'$or': [{'clientId': ?0}, {'isGlobal': true}]}", count = true)
    long countByClientIdOrIsGlobal(String clientId);

    boolean existsByClientIdAndName(String clientId, String name);

    @Query(value = "{'landingPageId': ?0}", exists = true)
    boolean isPageUsedInCampaign(String pageId);
}
