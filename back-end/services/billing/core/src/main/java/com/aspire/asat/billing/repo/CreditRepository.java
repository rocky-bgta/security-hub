package com.aspire.asat.billing.repo;

import com.aspire.asat.billing.model.Credit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditRepository extends MongoRepository<Credit, String> {

    Optional<Credit> findByClientId(String clientId);

    boolean existsByClientId(String clientId);
}
