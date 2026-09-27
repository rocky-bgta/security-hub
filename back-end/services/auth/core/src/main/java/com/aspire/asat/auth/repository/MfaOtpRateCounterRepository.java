package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.MfaOtpRateCounter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MfaOtpRateCounterRepository extends MongoRepository<MfaOtpRateCounter, String> {
}
