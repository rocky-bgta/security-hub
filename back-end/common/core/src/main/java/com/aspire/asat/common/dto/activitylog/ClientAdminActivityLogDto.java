package com.aspire.asat.common.dto.activitylog;

import com.aspire.asat.common.enums.ActivityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientAdminActivityLogDto {
    private String activityId;
    private String clientAdminId;
    private String clientAdminName;
    private String clientAdminEmail;
    private String organizationName;
    private ActivityType actionPerformed;
    private String actionDescription;
    private Instant timestamp;
    private String status;
    private String ipAddress;
    private String details;
    private String oldValue;
    private String newValue;
}

