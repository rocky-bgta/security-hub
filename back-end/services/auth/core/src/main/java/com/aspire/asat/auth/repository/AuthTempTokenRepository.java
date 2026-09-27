package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.AuthTempToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthTempTokenRepository extends MongoRepository<AuthTempToken, String> {

    void deleteAllByUserId(String userId);
}
