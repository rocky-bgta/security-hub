package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.VishingAttackTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository for platform vishing attack templates.
 */
@Repository
public interface VishingAttackTemplateRepository extends MongoRepository<VishingAttackTemplate, String> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    Page<VishingAttackTemplate> searchByName(String search, Pageable pageable);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' } }", count = true)
    long countSearchByName(String search);
}
