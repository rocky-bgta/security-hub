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
public class ClientAdminActivityLogRequestDto {
    private String clientAdminId;
    private String search;
    private ActivityType activityType;
    private String status;
    private String ipAddress;
    private Instant startDate;
    private Instant endDate;
    private Integer offset;
    private Integer pageSize;
    private String sortBy;
    private String order;
}

