package com.aspire.asat.billing.repo;

import com.aspire.asat.billing.model.NextStep;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NextStepRepository extends MongoRepository<NextStep, String> {

    Optional<NextStep> findByName(String name);

    boolean existsByName(String name);
}

