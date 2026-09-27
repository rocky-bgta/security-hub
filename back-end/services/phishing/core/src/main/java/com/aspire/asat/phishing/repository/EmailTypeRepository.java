package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.EmailType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository for EmailType configuration entries.
 */
@Repository
public interface EmailTypeRepository extends MongoRepository<EmailType, String> {

    Page<EmailType> findByIsActive(Boolean isActive, Pageable pageable);

    long countByIsActive(Boolean isActive);

    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': ?1 }")
    Page<EmailType> searchByNameAndIsActive(String search, Boolean isActive, Pageable pageable);

    @Query(value = "{ 'name': { $regex: ?0, $options: 'i' }, 'isActive': ?1 }", count = true)
    long countSearchByNameAndIsActive(String search, Boolean isActive);
}

