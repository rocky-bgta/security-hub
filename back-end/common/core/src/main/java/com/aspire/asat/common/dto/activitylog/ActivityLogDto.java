package com.aspire.asat.common.dto.activitylog;

import com.aspire.asat.common.enums.ActivityStatus;
import com.aspire.asat.common.enums.ActivityType;
import com.aspire.asat.common.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogDto {
    private String activityId;
    private String userId;
    private String countryId;
    private String aspireAdminId;
    private String mspId;
    private String clientAdminId;
    private UserType userType;
    private String userName;
    private String userEmail;
    private String fullName;
    private ActivityType activityType;
    private String activityDescription;
    private ActivityStatus activityStatus;
    private Instant timestamp;
    private String ipAddress;
    private String description;
    private String oldValue;
    private String newValue;
}

