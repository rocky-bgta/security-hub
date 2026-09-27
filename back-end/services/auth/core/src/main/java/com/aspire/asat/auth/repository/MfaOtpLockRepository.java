package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.MfaOtpLock;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MfaOtpLockRepository extends MongoRepository<MfaOtpLock, UUID> {
}
