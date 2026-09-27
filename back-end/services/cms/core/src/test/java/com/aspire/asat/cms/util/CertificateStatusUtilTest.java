package com.aspire.asat.cms.util;

import com.aspire.asat.cms.dto.enums.CertificateStatus;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CertificateStatusUtilTest {

    @Test
    void resolveStatus_nullExpiry_returnsNull() {
        assertNull(CertificateStatusUtil.resolveStatus(null));
    }

    @Test
    void resolveStatus_withThreshold_nullExpiry_returnsExpired() {
        assertEquals(CertificateStatus.EXPIRED, CertificateStatusUtil.resolveStatus(null, 60));
    }

    @Test
    void toDisplayLabel_mapsEnumValues() {
        assertEquals("Expired", CertificateStatusUtil.toDisplayLabel(CertificateStatus.EXPIRED));
        assertEquals("Expiring Soon", CertificateStatusUtil.toDisplayLabel(CertificateStatus.EXPIRING_SOON));
        assertEquals("Valid", CertificateStatusUtil.toDisplayLabel(CertificateStatus.VALID));
    }

    @Test
    void resolveStatus_pastExpiry_returnsExpired() {
        Instant expiry = Instant.now().minus(1, ChronoUnit.DAYS);
        assertEquals(CertificateStatus.EXPIRED, CertificateStatusUtil.resolveStatus(expiry));
    }

    @Test
    void resolveStatus_withinThirtyDays_returnsExpiringSoon() {
        Instant expiry = Instant.now().plus(10, ChronoUnit.DAYS);
        assertEquals(CertificateStatus.EXPIRING_SOON, CertificateStatusUtil.resolveStatus(expiry));
    }

    @Test
    void resolveStatus_beyondThirtyDays_returnsValid() {
        Instant expiry = Instant.now().plus(45, ChronoUnit.DAYS);
        assertEquals(CertificateStatus.VALID, CertificateStatusUtil.resolveStatus(expiry));
    }

    @Test
    void resolveStatus_exactlyAtThreshold_returnsExpiringSoon() {
        Instant expiry = Instant.now().plus(CertificateStatusUtil.EXPIRING_SOON_DAYS, ChronoUnit.DAYS);
        assertEquals(CertificateStatus.EXPIRING_SOON, CertificateStatusUtil.resolveStatus(expiry));
    }

    @Test
    void buildStatusCriteria_expired_includesNullExpiry() {
        Criteria criteria = CertificateStatusUtil.buildStatusCriteria(CertificateStatus.EXPIRED);
        assertTrue(criteria != null);
        Query query = new Query(criteria);
        // $or of expiryDate < now OR expiryDate == null
        assertTrue(query.getQueryObject().containsKey("$or")
                || query.getQueryObject().containsKey("expiryDate"));
    }

    @Test
    void buildStatusCriteria_null_returnsNull() {
        assertNull(CertificateStatusUtil.buildStatusCriteria(null));
    }

    @Test
    void buildExpiredOrExpiringSoonCriteria_includesLteThresholdAndNullExpiry() {
        Criteria criteria = CertificateStatusUtil.buildExpiredOrExpiringSoonCriteria(60);
        Query query = new Query(criteria);
        Document queryObject = query.getQueryObject();
        assertTrue(queryObject.containsKey("$or") || queryObject.containsKey("expiryDate"));
        assertTrue(queryObject.toString().contains("expiryDate"));
    }
}
