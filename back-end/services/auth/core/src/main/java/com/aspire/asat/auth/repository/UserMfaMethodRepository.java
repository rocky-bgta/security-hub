package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.UserMfaMethod;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserMfaMethodRepository extends MongoRepository<UserMfaMethod, UUID> {

    List<UserMfaMethod> findByUserId(UUID userId);

    Optional<UserMfaMethod> findByUserIdAndMethod(UUID userId, String method);

    Optional<UserMfaMethod> findByUserIdAndIsDefaultTrue(UUID userId);
}
