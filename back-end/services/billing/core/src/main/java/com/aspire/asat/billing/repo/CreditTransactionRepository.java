package com.aspire.asat.billing.repo;

import com.aspire.asat.billing.model.CreditTransaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditTransactionRepository extends MongoRepository<CreditTransaction, String> {

    List<CreditTransaction> findByClientIdOrderByCreatedAtDesc(String clientId);

    List<CreditTransaction> findByReferenceTypeAndReferenceId(String referenceType, String referenceId);
}
