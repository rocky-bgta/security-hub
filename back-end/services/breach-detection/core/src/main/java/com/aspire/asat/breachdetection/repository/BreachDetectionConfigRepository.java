package com.aspire.asat.breachdetection.repository;

import com.aspire.asat.breachdetection.model.BreachDetectionConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BreachDetectionConfigRepository extends MongoRepository<BreachDetectionConfig, String> {
    Optional<BreachDetectionConfig> findByClientId(String clientId);

    @Query("{ 'collectBreachData': true }")
    List<BreachDetectionConfig> findAllWithCollectionEnabled();
}
