package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.password.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUsernameAndUsedFalse(String username);
}

