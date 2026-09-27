package com.aspire.asat.cms.dto.client.responseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhishingCourseStatisticsCountsDto {

    private long totalUsers;
    private long completedUsers;
    private long inProgressUsers;
    private long pendingUsers;
    private long expiredUsers;
}
