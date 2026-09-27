package com.aspire.asat.breachdetection.repository;

import com.aspire.asat.breachdetection.model.RecipientBreach;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipientBreachRepository extends MongoRepository<RecipientBreach, String> {
    Optional<RecipientBreach> findByClientIdAndBreachRecordIdAndEmail(String clientId, String breachRecordId, String email);
}
