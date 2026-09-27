package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.TierConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TierConfigurationRepository extends MongoRepository<TierConfiguration, String> {

    boolean existsByTierNameIgnoreCase(String tierName);
}
