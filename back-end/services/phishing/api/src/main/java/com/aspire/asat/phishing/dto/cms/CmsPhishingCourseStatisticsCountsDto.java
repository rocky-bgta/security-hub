package com.aspire.asat.phishing.dto.cms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmsPhishingCourseStatisticsCountsDto {

    private long totalUsers;
    private long completedUsers;
    private long inProgressUsers;
    private long pendingUsers;
    private long expiredUsers;
}
