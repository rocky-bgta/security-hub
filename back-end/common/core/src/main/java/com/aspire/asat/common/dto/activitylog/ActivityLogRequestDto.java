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
public class ActivityLogRequestDto {
    private String search;
    private String countryId;
    private String aspireAdminId;
    private String mspId;
    private String clientAdminId;
    private String user;
    private String userId;
    private ActivityType activityType;
    private ActivityStatus activityStatus;
    private UserType userType;
    private String ipAddress;
    private Instant startDate;
    private Instant endDate;
    private Integer offset;
    private Integer pageSize;
    private String sortBy;
    private String order;
}

