package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.BreachDetectionConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for breach detection configuration.
 */
@Repository
public interface BreachDetectionConfigRepository extends MongoRepository<BreachDetectionConfig, String> {

    /**
     * Find by client ID
     */
    Optional<BreachDetectionConfig> findByClientId(String clientId);

    /**
     * Find all configs with breach collection enabled
     */
    @Query("{ 'collectBreachData': true }")
    List<BreachDetectionConfig> findAllWithCollectionEnabled();

    /**
     * Check if config exists for client
     */
    boolean existsByClientId(String clientId);
}
