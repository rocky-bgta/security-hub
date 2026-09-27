package com.aspire.asat.cms.util;

import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

public final class PackageAssignmentStatusUtil {

    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_EXPIRING = "Expiring";
    public static final String STATUS_EXPIRED = "Expired";
    public static final String STATUS_COMPLETE = "Complete";

    public static final double EXPIRING_PROGRESS_RATIO = 0.8;

    private static final Set<String> COMPLETED_STATUSES = Set.of("COMPLETED");

    private PackageAssignmentStatusUtil() {
    }

    public static String resolveDisplayStatus(LocalDate assignedDate,
                                              LocalDate expiryDate,
                                              String enrollmentStatus,
                                              LocalDate today) {
        if (isComplete(enrollmentStatus)) {
            return STATUS_COMPLETE;
        }
        if (isExpired(expiryDate, today)) {
            return STATUS_EXPIRED;
        }
        if (isExpiringSoon(assignedDate, expiryDate, today)) {
            return STATUS_EXPIRING;
        }
        return STATUS_ACTIVE;
    }

    public static boolean isComplete(String enrollmentStatus) {
        return enrollmentStatus != null && COMPLETED_STATUSES.contains(enrollmentStatus.toUpperCase());
    }

    public static boolean isExpired(LocalDate expiryDate, LocalDate today) {
        return expiryDate != null && expiryDate.isBefore(today);
    }

    public static boolean isExpiringSoon(LocalDate assignedDate, LocalDate expiryDate, LocalDate today) {
        if (assignedDate == null || expiryDate == null || isExpired(expiryDate, today)) {
            return false;
        }
        if (today.isBefore(assignedDate) || today.isAfter(expiryDate)) {
            return false;
        }
        long totalDays = ChronoUnit.DAYS.between(assignedDate, expiryDate);
        if (totalDays <= 0) {
            return false;
        }
        long elapsedDays = ChronoUnit.DAYS.between(assignedDate, today);
        return ((double) elapsedDays / totalDays) >= EXPIRING_PROGRESS_RATIO;
    }

    public static boolean isActive(LocalDate assignedDate, LocalDate expiryDate, LocalDate today) {
        if (isExpired(expiryDate, today) || isExpiringSoon(assignedDate, expiryDate, today)) {
            return false;
        }
        if (assignedDate == null || expiryDate == null) {
            return true;
        }
        return !today.isBefore(assignedDate) && !today.isAfter(expiryDate);
    }

    public static Criteria buildExpiryStatusCriteria(String statusFilter, LocalDate today) {
        String normalized = normalizeStatusFilter(statusFilter);
        if (normalized == null) {
            return new Criteria();
        }
        return switch (normalized) {
            case "COMPLETE" -> Criteria.where("status").is("COMPLETED");
            case "EXPIRED" -> new Criteria().andOperator(
                    Criteria.where("status").nin(COMPLETED_STATUSES),
                    Criteria.where("expiryDate").lt(today));
            case "EXPIRING" -> new Criteria().andOperator(
                    Criteria.where("status").nin(COMPLETED_STATUSES),
                    expiringSoonExprCriteria(today));
            case "ACTIVE" -> new Criteria().andOperator(
                    Criteria.where("status").nin(COMPLETED_STATUSES),
                    activeExprCriteria(today));
            default -> new Criteria();
        };
    }

    /**
     * Maps UI/API aliases onto ACTIVE, EXPIRING, EXPIRED, or COMPLETE.
     * Unknown values return {@code null} (no status filter).
     */
    static String normalizeStatusFilter(String statusFilter) {
        if (statusFilter == null || statusFilter.isBlank()) {
            return null;
        }
        String key = statusFilter.trim().toUpperCase().replace('-', '_').replaceAll("\\s+", "_");
        return switch (key) {
            case "COMPLETE", "COMPLETED" -> "COMPLETE";
            case "EXPIRED" -> "EXPIRED";
            case "EXPIRING", "EXPIRING_SOON", "EXPIRINGSOON" -> "EXPIRING";
            case "ACTIVE" -> "ACTIVE";
            default -> null;
        };
    }

    private static Criteria expiringSoonExprCriteria(LocalDate today) {
        Date todayDate = toDate(today);
        Document totalDuration = new Document("$subtract", Arrays.asList("$expiryDate", "$assignedDate"));
        Document elapsed = new Document("$subtract", Arrays.asList(todayDate, "$assignedDate"));
        Document threshold = new Document("$multiply", Arrays.asList(EXPIRING_PROGRESS_RATIO, totalDuration));

        Document expr = new Document("$and", List.of(
                new Document("$ne", Arrays.asList("$assignedDate", null)),
                new Document("$ne", Arrays.asList("$expiryDate", null)),
                new Document("$gt", Arrays.asList(totalDuration, 0)),
                new Document("$gte", Arrays.asList("$expiryDate", todayDate)),
                new Document("$gte", Arrays.asList(todayDate, "$assignedDate")),
                new Document("$lte", Arrays.asList(todayDate, "$expiryDate")),
                new Document("$gte", Arrays.asList(elapsed, threshold))
        ));
        return Criteria.where("$expr").is(expr);
    }

    private static Criteria activeExprCriteria(LocalDate today) {
        Date todayDate = toDate(today);
        Document totalDuration = new Document("$subtract", Arrays.asList("$expiryDate", "$assignedDate"));
        Document elapsed = new Document("$subtract", Arrays.asList(todayDate, "$assignedDate"));
        Document threshold = new Document("$multiply", Arrays.asList(EXPIRING_PROGRESS_RATIO, totalDuration));

        Document inWindowWithProgress = new Document("$and", List.of(
                new Document("$ne", Arrays.asList("$assignedDate", null)),
                new Document("$ne", Arrays.asList("$expiryDate", null)),
                new Document("$gt", Arrays.asList(totalDuration, 0)),
                new Document("$gte", Arrays.asList("$expiryDate", todayDate)),
                new Document("$gte", Arrays.asList(todayDate, "$assignedDate")),
                new Document("$lte", Arrays.asList(todayDate, "$expiryDate")),
                new Document("$lt", Arrays.asList(elapsed, threshold))
        ));

        Document expr = new Document("$or", List.of(
                new Document("$eq", Arrays.asList("$assignedDate", null)),
                new Document("$eq", Arrays.asList("$expiryDate", null)),
                new Document("$lte", Arrays.asList(totalDuration, 0)),
                inWindowWithProgress
        ));
        return Criteria.where("$expr").is(expr);
    }

    private static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneOffset.UTC).toInstant());
    }
}
