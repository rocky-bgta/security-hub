package com.aspire.asat.cms.util;

import com.aspire.asat.cms.dto.enums.CertificateStatus;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class CertificateStatusUtil {

    public static final int EXPIRING_SOON_DAYS = 30;

    private CertificateStatusUtil() {
    }

    public static CertificateStatus resolveStatus(Instant expiryDate) {
        if (expiryDate == null) {
            return null;
        }
        Instant now = Instant.now();
        Instant threshold = now.plus(EXPIRING_SOON_DAYS, ChronoUnit.DAYS);
        if (expiryDate.isBefore(now)) {
            return CertificateStatus.EXPIRED;
        }
        if (!expiryDate.isAfter(threshold)) {
            return CertificateStatus.EXPIRING_SOON;
        }
        return CertificateStatus.VALID;
    }

    public static Criteria buildStatusCriteria(CertificateStatus status) {
        return buildStatusCriteria(status, EXPIRING_SOON_DAYS);
    }

    public static Criteria buildStatusCriteria(CertificateStatus status, int thresholdDays) {
        if (status == null) {
            return null;
        }
        int effectiveThreshold = thresholdDays > 0 ? thresholdDays : EXPIRING_SOON_DAYS;
        Instant now = Instant.now();
        Instant threshold = now.plus(effectiveThreshold, ChronoUnit.DAYS);
        return switch (status) {
            case EXPIRED -> new Criteria().orOperator(
                    Criteria.where("expiryDate").lt(now),
                    Criteria.where("expiryDate").isNull());
            case EXPIRING_SOON -> new Criteria().andOperator(
                    Criteria.where("expiryDate").gte(now),
                    Criteria.where("expiryDate").lte(threshold));
            case VALID -> Criteria.where("expiryDate").gt(threshold);
        };
    }

    public static Criteria buildExpiredOrExpiringSoonCriteria(int thresholdDays) {
        int effectiveThreshold = thresholdDays > 0 ? thresholdDays : EXPIRING_SOON_DAYS;
        Instant threshold = Instant.now().plus(effectiveThreshold, ChronoUnit.DAYS);
        return new Criteria().orOperator(
                Criteria.where("expiryDate").lte(threshold),
                Criteria.where("expiryDate").isNull());
    }

    public static String toDisplayLabel(CertificateStatus status) {
        if (status == null) {
            return "Expired";
        }
        return switch (status) {
            case EXPIRED -> "Expired";
            case EXPIRING_SOON -> "Expiring Soon";
            case VALID -> "Valid";
        };
    }

    public static CertificateStatus resolveStatus(Instant expiryDate, int thresholdDays) {
        if (expiryDate == null) {
            return CertificateStatus.EXPIRED;
        }
        int effectiveThreshold = thresholdDays > 0 ? thresholdDays : EXPIRING_SOON_DAYS;
        Instant now = Instant.now();
        Instant threshold = now.plus(effectiveThreshold, ChronoUnit.DAYS);
        if (expiryDate.isBefore(now)) {
            return CertificateStatus.EXPIRED;
        }
        if (!expiryDate.isAfter(threshold)) {
            return CertificateStatus.EXPIRING_SOON;
        }
        return CertificateStatus.VALID;
    }
}
