package com.aspire.asat.registration.repository;

import com.aspire.asat.common.enums.ActivityType;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ActivityLogRepository extends MongoRepository<ActivityLog, String> {

    Page<ActivityLog> findByUserId(String userId, Pageable pageable);

    Page<ActivityLog> findByUserType(UserType userType, Pageable pageable);

    Page<ActivityLog> findByActivityType(ActivityType activityType, Pageable pageable);

    List<ActivityLog> findByCreatedAtBetween(Instant startDate, Instant endDate);

    Page<ActivityLog> findByUserIdAndActivityType(String userId, ActivityType activityType, Pageable pageable);
}