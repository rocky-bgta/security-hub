package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.NetTermConfiguration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NetTermConfigurationRepository extends MongoRepository<NetTermConfiguration, String> {

    boolean existsByNetTermNameIgnoreCase(String netTermName);
}

