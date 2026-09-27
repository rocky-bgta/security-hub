package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.dto.common.MonthlyActivityDurationDTO;
import com.aspire.asat.cms.service.UserActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserActivityController {

    @Autowired
    private UserActivityService userActivityService;

    @PostMapping("/update-activity")
    public void updateActivity(@RequestParam String userId, @RequestParam String sessionId) {
        // Whenever frontend sends a request, update the lastAvailableTime
        userActivityService.updateActivity(userId, sessionId);
    }

    // API endpoint to get the total duration for each month (January to December) for a specific userId
    @GetMapping("/total-duration-per-year")
    public MonthlyActivityDurationDTO getTotalDurationPerYear(@RequestParam String userId) {
        // Call the service to get the total duration for the given userId
        return userActivityService.getTotalDurationForYear(userId);
    }

    @GetMapping("/total-duration-last-7-days")
    public long getTotalDurationLast7Days(@RequestParam String userId) {
        return userActivityService.getTotalDurationForLast7Days(userId);
    }


    // API endpoint to get the total duration from the current date to the past 30 days for a specific userId
    @GetMapping("/total-duration-last-30-days")
    public long getTotalDurationLast30Days(@RequestParam String userId) {
        return userActivityService.getTotalDurationForLast30Days(userId);
    }
}
