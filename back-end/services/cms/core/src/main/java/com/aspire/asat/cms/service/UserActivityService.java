package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.common.MonthlyActivityDurationDTO;


public interface UserActivityService {
    void updateActivity(String userId, String sessionId);
    MonthlyActivityDurationDTO getTotalDurationForYear(String userId);
    long getTotalDurationForLast7Days(String userId);
    long getTotalDurationForLast30Days(String userId);
}
