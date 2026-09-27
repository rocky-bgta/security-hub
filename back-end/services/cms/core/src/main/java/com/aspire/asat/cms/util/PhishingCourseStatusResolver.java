package com.aspire.asat.cms.util;

import com.aspire.asat.cms.dto.enums.PhishingCourseDashboardStatus;
import com.aspire.asat.cms.model.UserSubPackage;

import java.time.LocalDate;
import java.util.Set;

public final class PhishingCourseStatusResolver {

    private static final Set<String> COMPLETED_STATUSES = Set.of("COMPLETED", "PHISHING_TRAINING_COMPLETED");
    private static final Set<String> IN_PROGRESS_STATUSES = Set.of("IN_PROGRESS", "EXAM");

    private PhishingCourseStatusResolver() {
    }

    public static PhishingCourseDashboardStatus resolve(UserSubPackage enrollment) {
        if (enrollment == null) {
            return PhishingCourseDashboardStatus.pending;
        }

        String status = enrollment.getStatus();
        if (status != null && COMPLETED_STATUSES.contains(status.toUpperCase())) {
            return PhishingCourseDashboardStatus.complete;
        }

        if (isExpired(enrollment)) {
            return PhishingCourseDashboardStatus.expired;
        }

        if (status != null && IN_PROGRESS_STATUSES.contains(status.toUpperCase())) {
            return PhishingCourseDashboardStatus.InProgress;
        }

        return PhishingCourseDashboardStatus.pending;
    }

    public static boolean isExpired(UserSubPackage enrollment) {
        if (enrollment == null || enrollment.getExpiryDate() == null) {
            return false;
        }
        String status = enrollment.getStatus();
        if (status != null && COMPLETED_STATUSES.contains(status.toUpperCase())) {
            return false;
        }
        return enrollment.getExpiryDate().isBefore(LocalDate.now());
    }
}
