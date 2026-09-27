package com.aspire.asat.phishing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Public phishing course statistics payload returned by
 * {@code GET /api/v1/phishing/phishing-course/statistics}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhishingCourseStatisticsDto {

    private long totalUsers;

    private long completedUsers;
    private long completePercentage;

    private long inProgressUsers;
    private long inProgressPercentage;

    private long pendingUsers;
    private long pendingPercentage;

    private long expiredUsers;
    private long expiredPercentage;
}
