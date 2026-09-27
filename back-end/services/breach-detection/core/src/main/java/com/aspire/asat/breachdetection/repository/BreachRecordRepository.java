package com.aspire.asat.breachdetection.repository;

import com.aspire.asat.breachdetection.model.BreachRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BreachRecordRepository extends MongoRepository<BreachRecord, String> {
    Optional<BreachRecord> findByClientIdAndExternalBreachId(String clientId, String externalBreachId);
}
