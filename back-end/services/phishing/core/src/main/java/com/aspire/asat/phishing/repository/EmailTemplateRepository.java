package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.DifficultyLevel;
import com.aspire.asat.phishing.model.EmailTemplate;
import com.aspire.asat.phishing.repository.custom.EmailTemplateRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for EmailTemplate entity operations.
 */
@Repository
public interface EmailTemplateRepository extends MongoRepository<EmailTemplate, String>, EmailTemplateRepositoryCustom {
    
    /**
     * Find templates accessible by a client (client-specific + global templates)
     */
    @Query("{'$or': [{'clientId': ?0}, {'isGlobal': true}]}")
    Page<EmailTemplate> findByClientIdOrGlobal(String clientId, Pageable pageable);
    
    /**
     * Find templates by client with filters
     */
    @Query("{'$and': [" +
           "{'$or': [{'clientId': ?0}, {'isGlobal': true}]}," +
           "{'$or': [" +
           "  {'templateName': {'$regex': ?1, '$options': 'i'}}," +
           "  {'emailSubject': {'$regex': ?1, '$options': 'i'}}," +
           "  {'tags': {'$regex': ?1, '$options': 'i'}}" +
           "]}" +
           "]}")
    Page<EmailTemplate> searchByClientIdAndKeyword(String clientId, String keyword, Pageable pageable);
    
    /**
     * Find template by ID and client (or global)
     */
    @Query("{'$and': [{'_id': ?0}, {'$or': [{'clientId': ?1}, {'isGlobal': true}]}]}")
    Optional<EmailTemplate> findByIdAndClientIdOrGlobal(String id, String clientId);
    
    /**
     * Count templates matching search for a client
     */
    @Query(value = "{'$and': [" +
           "{'$or': [{'clientId': ?0}, {'isGlobal': true}]}," +
           "{'$or': [" +
           "  {'templateName': {'$regex': ?1, '$options': 'i'}}," +
           "  {'emailSubject': {'$regex': ?1, '$options': 'i'}}," +
           "  {'tags': {'$regex': ?1, '$options': 'i'}}" +
           "]}" +
           "]}", count = true)
    long countByClientIdAndKeyword(String clientId, String keyword);
    
    /**
     * Find templates by difficulty level
     */
    @Query("{'$and': [{'$or': [{'clientId': ?0}, {'isGlobal': true}]}, {'difficultyLevel': ?1}]}")
    Page<EmailTemplate> findByClientIdAndDifficultyLevel(String clientId, DifficultyLevel level, Pageable pageable);
    
    /**
     * Find templates by payload type
     */
    @Query("{'$and': [{'$or': [{'clientId': ?0}, {'isGlobal': true}]}, {'payloadType': ?1}]}")
    Page<EmailTemplate> findByClientIdAndPayloadType(String clientId, String type, Pageable pageable);
    
    /**
     * Find templates by language
     */
    @Query("{'$and': [{'$or': [{'clientId': ?0}, {'isGlobal': true}]}, {'language': ?1}]}")
    Page<EmailTemplate> findByClientIdAndLanguage(String clientId, String language, Pageable pageable);
    
    /**
     * Find distinct locations used in templates
     */
    @Query(value = "{'$or': [{'clientId': ?0}, {'isGlobal': true}]}", fields = "{'serviceLocation': 1}")
    List<EmailTemplate> findDistinctLocationsByClientId(String clientId);
    
    /**
     * Find distinct languages used in templates
     */
    @Query(value = "{'$or': [{'clientId': ?0}, {'isGlobal': true}]}", fields = "{'language': 1}")
    List<EmailTemplate> findDistinctLanguagesByClientId(String clientId);
    
    /**
     * Count templates accessible by a client (client-specific + global)
     */
    @Query(value = "{'$or': [{'clientId': ?0}, {'isGlobal': true}]}", count = true)
    long countByClientIdOrIsGlobal(String clientId);

    @Query(value = "{'$and': [" +
           "{'$or': [{'clientId': ?0}, {'isGlobal': true}]}," +
           "{'$or': [{'templateType': 'EMAIL'}, {'templateType': {$exists: false}}, {'templateType': null}]}" +
           "]}", count = true)
    long countEmailTemplatesByClientIdOrIsGlobal(String clientId);

    @Query(value = "{'$and': [{'$or': [{'clientId': ?0}, {'isGlobal': true}]}, {'templateType': 'SMS'}]}", count = true)
    long countSmsTemplatesByClientIdOrIsGlobal(String clientId);
    
    /**
     * Check if template name exists for client
     */
    boolean existsByClientIdAndTemplateName(String clientId, String templateName);
    
    /**
     * Check if template is used in any active campaign
     */
    @Query(value = "{'emailTemplateId': ?0}", exists = true)
    boolean isTemplateUsedInCampaign(String templateId);
    
    /**
     * Increment popularity count
     */
    @Query("{'_id': ?0}")
    void incrementPopularity(String templateId);

    /**
     * Find templates that have the given landing page ID in their bindings.
     */
    @Query("{'landingPageIds': ?0}")
    List<EmailTemplate> findByLandingPageIdsContaining(String landingPageId);

    /**
     * Find templates bound to a landing page, scoped to client-accessible templates.
     */
    @Query("{'$and': [{'landingPageIds': ?0}, {'$or': [{'clientId': ?1}, {'isGlobal': true}]}]}")
    List<EmailTemplate> findByLandingPageIdsContainingAndClientIdOrGlobal(String landingPageId, String clientId);
}
