package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.MfaOtpLifecycle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MfaOtpLifecycleRepository extends MongoRepository<MfaOtpLifecycle, UUID> {

    Optional<MfaOtpLifecycle> findByUserIdAndMethod(UUID userId, String method);
}
