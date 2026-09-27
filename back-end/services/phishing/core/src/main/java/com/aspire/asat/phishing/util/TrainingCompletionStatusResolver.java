package com.aspire.asat.phishing.util;

import java.util.Locale;
import java.util.Set;

/**
 * Aligns phishing campaign training-completion checks with CMS {@code SubPackageStatus}
 * and {@code PhishingCourseDashboardStatus} semantics.
 *
 * <p>CMS stores raw statuses such as {@code COMPLETED} (post-exam) and
 * {@code PHISHING_TRAINING_COMPLETED} (default phishing training flow without exam).
 * The phishing-course details API normalizes both to dashboard {@code complete}, but
 * recipients may also carry raw CMS values depending on integration path or API changes.
 */
public final class TrainingCompletionStatusResolver {

    /** Dashboard status from CMS {@code GET /client/phishing-course/details}. */
    public static final String DASHBOARD_COMPLETE = "complete";

    /** Raw CMS completion after exam flow. */
    public static final String CMS_COMPLETED = "COMPLETED";

    /** Raw CMS completion for default phishing training flow (no exam). */
    public static final String CMS_PHISHING_TRAINING_COMPLETED = "PHISHING_TRAINING_COMPLETED";

    private static final Set<String> COMPLETED_STATUSES_UPPER = Set.of(
            DASHBOARD_COMPLETE.toUpperCase(Locale.ROOT),
            CMS_COMPLETED,
            CMS_PHISHING_TRAINING_COMPLETED
    );

    private TrainingCompletionStatusResolver() {
    }

    /**
     * @return {@code true} when the given training status represents a finished enrollment.
     */
    public static boolean isCompleted(String trainingStatus) {
        if (trainingStatus == null || trainingStatus.isBlank()) {
            return false;
        }
        return COMPLETED_STATUSES_UPPER.contains(trainingStatus.trim().toUpperCase(Locale.ROOT));
    }
}
