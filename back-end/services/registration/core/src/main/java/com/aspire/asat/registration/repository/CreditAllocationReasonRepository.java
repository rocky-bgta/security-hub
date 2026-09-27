package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.CreditAllocationReason;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditAllocationReasonRepository extends MongoRepository<CreditAllocationReason, String> {

    boolean existsByReasonNameIgnoreCase(String reasonName);
}

