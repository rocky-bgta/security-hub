package com.aspire.asat.auth.repository;

import com.aspire.asat.auth.entity.password.UserPasswordHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPasswordHistoryRepository extends MongoRepository<UserPasswordHistory, String> {

    /**
     * Last N password hashes for the user (newest first).
     */
    List<UserPasswordHistory> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
