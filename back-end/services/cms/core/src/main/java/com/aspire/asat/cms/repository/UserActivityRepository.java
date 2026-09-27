package com.aspire.asat.cms.repository;

import com.aspire.asat.cms.model.UserActivity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserActivityRepository extends MongoRepository<UserActivity, String> {
    Optional<UserActivity> findByUserIdAndSessionId(String userId, String sessionId);
    @Query("{'lastAvailableTime': {$lt: ?0}}")
    List<UserActivity> findInactiveSessions(LocalDateTime threshold);
}
