package com.aspire.asat.registration.model;

import com.aspire.asat.common.enums.ActivityStatus;
import com.aspire.asat.common.enums.ActivityType;
import com.aspire.asat.common.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "activity_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {

    @Id
    private String id;

    private String userId;
    private String countryId;
    private String aspireAdminId;
    private String mspId;
    private String clientAdminId;

    private String email;

    private String username;

    private String fullName;

    private UserType userType;

    private ActivityType activityType;

    private ActivityStatus activityStatus;

    private String description;

    private String oldValue;

    private String newValue;

    private String ipAddress;

    private Instant createdAt;
}

