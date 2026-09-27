package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.UserSuspendReason;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSuspendReasonRepository extends MongoRepository<UserSuspendReason, String> {

    /**
     * Find all suspend reasons for a specific user
     *
     * @param userId The user ID
     * @return List of UserSuspendReason records for the user
     */
    List<UserSuspendReason> findByUserId(String userId);

    /**
     * Find the most recent suspend reason for a user
     *
     * @param userId The user ID
     * @return Optional UserSuspendReason (most recent)
     */
    Optional<UserSuspendReason> findFirstByUserIdOrderByCreatedAtDesc(String userId);
}

