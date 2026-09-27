package com.aspire.asat.registration.service;

import com.aspire.asat.common.dto.activitylog.*;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.registration.data.*;
import com.aspire.asat.common.enums.ActivityStatus;
import com.aspire.asat.common.enums.ActivityType;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface ActivityLogService {

    ActivityLog logActivity(ActivityLog activityLog);

    ActivityLog logActivity(String userId, UserType userType,
                            ActivityType activityType, ActivityStatus activityStatus, String description, String ipAddress);

    ActivityLog logActivity(String userId, String email, String username, String fullName, UserType userType,
                            ActivityType activityType, ActivityStatus activityStatus, String description, String ipAddress);

    ActivityLog logActivity(String userId, UserType userType,
                            ActivityType activityType, ActivityStatus activityStatus, String description, String oldValue, String newValue, String ipAddress);

    ActivityLog logActivity(String userId, String email, String username, String fullName, UserType userType,
                            ActivityType activityType, ActivityStatus activityStatus, String description, String oldValue, String newValue, String ipAddress);

    Page<ActivityLog> getActivitiesByUserId(String userId, Pageable pageable);

    Page<ActivityLog> getActivitiesByUserType(UserType userType, Pageable pageable);

    Page<ActivityLog> getActivitiesByActivityType(ActivityType activityType, Pageable pageable);

    List<ActivityLog> getActivitiesByDateRange(Instant startDate, Instant endDate);

    Page<ActivityLog> getAllActivities(Pageable pageable);

    Page<ActivityLog> getClientAdminActivityLogs(
            String clientAdminId,
            String search,
            ActivityType activityType,
            String status,
            String ipAddress,
            Instant startDate,
            Instant endDate,
            Pageable pageable);

    ActivityLog getActivityLogById(String activityId);

    ClientAdminActivityLogResponseDto getClientAdminActivityLogs(ClientAdminActivityLogRequestDto requestDto);

    ClientAdminActivityLogDto getClientAdminActivityLogById(String activityId);

    /**
     * Get activity logs with role-based access control.
     * USER: only own logs (userType=USER, userId=currentUser).
     * CLIENT_ADMIN: userType in (USER, CLIENT_ADMIN), clientAdminId=currentUser.
     * MSP: default scope mspId; clientAdminId=ALL → CLIENT_ADMIN logs under MSP (mspId=currentUserId);
     *      specific clientAdminId → CLIENT_ADMIN logs for that client.
     * ASPIRE_ADMIN/SUPER_ADMIN/SYSTEM_USER: all logs by default.
     *      mspId=ALL → userType=MSP; specific mspId → filter by mspId.
     *      clientAdminId=ALL → CLIENT_ADMIN under MSP; specific clientAdminId → that client.
     */
    ActivityLogResponseDto getActivityLogs(ActivityLogRequestDto requestDto, CurrentUserContext currentUserContext);
}
