package com.aspire.asat.cms.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageAssignmentStatusUtilTest {

    @Test
    void resolveDisplayStatus_shouldReturnCompleteForCompletedEnrollment() {
        LocalDate today = LocalDate.of(2026, 6, 20);
        String status = PackageAssignmentStatusUtil.resolveDisplayStatus(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 1),
                "COMPLETED",
                today);
        assertEquals(PackageAssignmentStatusUtil.STATUS_COMPLETE, status);
    }

    @Test
    void resolveDisplayStatus_shouldReturnExpiredWhenPastExpiryDate() {
        LocalDate today = LocalDate.of(2026, 6, 20);
        String status = PackageAssignmentStatusUtil.resolveDisplayStatus(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 6, 1),
                "IN_PROGRESS",
                today);
        assertEquals(PackageAssignmentStatusUtil.STATUS_EXPIRED, status);
    }

    @Test
    void resolveDisplayStatus_shouldReturnExpiringAt80PercentProgress() {
        LocalDate assigned = LocalDate.of(2026, 1, 1);
        LocalDate expiry = LocalDate.of(2027, 1, 1);
        LocalDate today = LocalDate.of(2026, 10, 20);
        String status = PackageAssignmentStatusUtil.resolveDisplayStatus(
                assigned, expiry, "IN_PROGRESS", today);
        assertEquals(PackageAssignmentStatusUtil.STATUS_EXPIRING, status);
    }

    @Test
    void resolveDisplayStatus_shouldReturnActiveBefore80PercentProgress() {
        LocalDate assigned = LocalDate.of(2026, 1, 1);
        LocalDate expiry = LocalDate.of(2027, 1, 1);
        LocalDate today = LocalDate.of(2026, 6, 1);
        String status = PackageAssignmentStatusUtil.resolveDisplayStatus(
                assigned, expiry, "IN_PROGRESS", today);
        assertEquals(PackageAssignmentStatusUtil.STATUS_ACTIVE, status);
    }

    @Test
    void resolveDisplayStatus_shouldFallbackToActiveWhenDatesMissing() {
        LocalDate today = LocalDate.of(2026, 6, 20);
        assertEquals(PackageAssignmentStatusUtil.STATUS_ACTIVE,
                PackageAssignmentStatusUtil.resolveDisplayStatus(null, null, "IN_PROGRESS", today));
        assertEquals(PackageAssignmentStatusUtil.STATUS_ACTIVE,
                PackageAssignmentStatusUtil.resolveDisplayStatus(
                        today.minusDays(10), today.plusDays(10), "IN_PROGRESS", today));
    }

    @Test
    void isComplete_shouldBeCaseInsensitive() {
        assertTrue(PackageAssignmentStatusUtil.isComplete("completed"));
        assertFalse(PackageAssignmentStatusUtil.isComplete("IN_PROGRESS"));
    }

    @Test
    void buildExpiryStatusCriteria_shouldBuildCompleteFilter() {
        Criteria criteria = PackageAssignmentStatusUtil.buildExpiryStatusCriteria("COMPLETE", LocalDate.now());
        assertTrue(criteria.getCriteriaObject().containsKey("status"));
    }

    @Test
    void buildExpiryStatusCriteria_shouldBuildExpiredFilterExcludingCompleted() {
        Criteria criteria = PackageAssignmentStatusUtil.buildExpiryStatusCriteria("EXPIRED", LocalDate.of(2026, 6, 20));
        assertTrue(criteria.getCriteriaObject().containsKey("$and"));
    }

    @Test
    void buildExpiryStatusCriteria_shouldReturnEmptyForBlankFilter() {
        Criteria criteria = PackageAssignmentStatusUtil.buildExpiryStatusCriteria(null, LocalDate.now());
        assertTrue(criteria.getCriteriaObject().isEmpty());
    }

    @Test
    void buildExpiryStatusCriteria_expiringSoonAlias_matchesExpiring() {
        LocalDate today = LocalDate.of(2026, 6, 20);
        Criteria expiring = PackageAssignmentStatusUtil.buildExpiryStatusCriteria("EXPIRING", today);
        Criteria soon = PackageAssignmentStatusUtil.buildExpiryStatusCriteria("EXPIRING_SOON", today);
        Criteria label = PackageAssignmentStatusUtil.buildExpiryStatusCriteria("Expiring Soon", today);

        assertTrue(expiring.getCriteriaObject().containsKey("$and"));
        assertEquals(expiring.getCriteriaObject(), soon.getCriteriaObject());
        assertEquals(expiring.getCriteriaObject(), label.getCriteriaObject());
    }

    @Test
    void buildExpiryStatusCriteria_completedAndDisplayLabels_normalize() {
        LocalDate today = LocalDate.of(2026, 6, 20);
        assertEquals(
                PackageAssignmentStatusUtil.buildExpiryStatusCriteria("COMPLETE", today).getCriteriaObject(),
                PackageAssignmentStatusUtil.buildExpiryStatusCriteria("COMPLETED", today).getCriteriaObject());
        assertEquals(
                PackageAssignmentStatusUtil.buildExpiryStatusCriteria("ACTIVE", today).getCriteriaObject(),
                PackageAssignmentStatusUtil.buildExpiryStatusCriteria("Active", today).getCriteriaObject());
        assertEquals(
                PackageAssignmentStatusUtil.buildExpiryStatusCriteria("EXPIRED", today).getCriteriaObject(),
                PackageAssignmentStatusUtil.buildExpiryStatusCriteria("Expired", today).getCriteriaObject());
    }

    @Test
    void buildExpiryStatusCriteria_unknownValue_returnsEmpty() {
        Criteria criteria = PackageAssignmentStatusUtil.buildExpiryStatusCriteria("UNKNOWN", LocalDate.now());
        assertTrue(criteria.getCriteriaObject().isEmpty());
    }
}
