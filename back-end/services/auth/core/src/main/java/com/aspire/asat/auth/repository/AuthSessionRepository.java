package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.AuthSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthSessionRepository extends MongoRepository<AuthSession, String> {

    List<AuthSession> findAllByUserId(String userId);

    void deleteAllByUserId(String userId);
}
