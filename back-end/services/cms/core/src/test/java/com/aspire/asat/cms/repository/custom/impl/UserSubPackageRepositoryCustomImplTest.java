package com.aspire.asat.cms.repository.custom.impl;

import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.UserSubPackage;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSubPackageRepositoryCustomImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private UserSubPackageRepositoryCustomImpl repository;

    @Captor
    private ArgumentCaptor<Query> queryCaptor;

    @Test
    void countByExpiryBucket_completeFilter_shouldIncludeCompletedStatusCriteria() {
        when(mongoTemplate.count(any(Query.class), eq(UserSubPackage.class), eq("user_subpackages")))
                .thenReturn(2L);

        repository.countByExpiryBucket("client-1", null, null, null, "COMPLETE");

        verify(mongoTemplate).count(queryCaptor.capture(), eq(UserSubPackage.class), eq("user_subpackages"));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsKey(queryObject, "status"));
        assertTrue(documentContainsValue(queryObject, "COMPLETED"));
        assertTrue(documentContainsKey(queryObject, "clientAdminId"));
    }

    @Test
    void countByExpiryBucket_expiredFilter_shouldExcludeCompletedAndUseExpiryDate() {
        when(mongoTemplate.count(any(Query.class), eq(UserSubPackage.class), eq("user_subpackages")))
                .thenReturn(1L);

        repository.countByExpiryBucket("client-1", null, null, null, "EXPIRED");

        verify(mongoTemplate).count(queryCaptor.capture(), eq(UserSubPackage.class), eq("user_subpackages"));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsKey(queryObject, "$and"));
        assertTrue(documentContainsKey(queryObject, "expiryDate"));
        assertTrue(documentContainsKey(queryObject, "status"));
    }

    @Test
    void countByExpiryBucket_expiringFilter_shouldUseExprCriteria() {
        when(mongoTemplate.count(any(Query.class), eq(UserSubPackage.class), eq("user_subpackages")))
                .thenReturn(3L);

        repository.countByExpiryBucket(null, List.of("client-1", "client-2"), null, null, "EXPIRING");

        verify(mongoTemplate).count(queryCaptor.capture(), eq(UserSubPackage.class), eq("user_subpackages"));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsKey(queryObject, "$expr"));
        assertTrue(documentContainsKey(queryObject, "clientAdminId"));
    }

    @Test
    void findAssignmentsForReport_shouldApplySearchAndStatusFilters() {
        when(mongoTemplate.find(any(Query.class), eq(UserSubPackage.class), eq("user_subpackages")))
                .thenReturn(List.of());

        repository.findAssignmentsForReport(
                "client-1", null, "ahmed", "ACTIVE",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 0, 20);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(UserSubPackage.class), eq("user_subpackages"));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsKey(queryObject, "subPackageName"));
        assertTrue(documentContainsKey(queryObject, "userEmail"));
        assertTrue(documentContainsKey(queryObject, "assignedDate"));
        assertTrue(documentContainsKey(queryObject, "$expr"));
    }

    @Test
    void findAssignmentsForReportExport_shouldUseExportPageSize() {
        when(mongoTemplate.find(any(Query.class), eq(UserSubPackage.class), eq("user_subpackages")))
                .thenReturn(List.of());

        repository.findAssignmentsForReportExport(
                "client-1", null, null, "COMPLETE", null, null, 0, 1000);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(UserSubPackage.class), eq("user_subpackages"));
        assertEqualsLimit(queryCaptor.getValue(), 1000);
    }

    @Test
    void countDistinctParentPackages_shouldResolveDistinctPackageIds() {
        UserSubPackage assignment = UserSubPackage.builder()
                .subPackageId("sub-1")
                .build();
        SubPackage subPackage = SubPackage.builder()
                .id("sub-1")
                .packageId("pkg-1")
                .build();

        when(mongoTemplate.find(any(Query.class), eq(UserSubPackage.class), eq("user_subpackages")))
                .thenReturn(List.of(assignment));
        when(mongoTemplate.find(any(Query.class), eq(SubPackage.class), eq("sub_packages")))
                .thenReturn(List.of(subPackage));

        long count = repository.countDistinctParentPackages("client-1", null, null, null);

        assertEquals(1L, count);
    }

    @Test
    void findLicenseAssignments_productAndUserFilters_appliedToQuery() {
        when(mongoTemplate.find(any(Query.class), eq(UserSubPackage.class), eq("user_subpackages")))
                .thenReturn(List.of());

        repository.findLicenseAssignments(
                "client-1", null, List.of("user-1"), "prod-1", null,
                "ACTIVE", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 1, 10);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(UserSubPackage.class), eq("user_subpackages"));
        Query query = queryCaptor.getValue();
        Document queryObject = query.getQueryObject();
        assertTrue(documentContainsKey(queryObject, "productId"));
        assertTrue(documentContainsKey(queryObject, "userId"));
        assertTrue(documentContainsKey(queryObject, "assignedDate"));
        assertEquals(10L, query.getSkip());
        assertEquals(10, query.getLimit());
    }

    @Test
    void countLicenseAssignments_emptyUserIds_returnsZeroWithoutQuery() {
        long count = repository.countLicenseAssignments(
                "client-1", null, List.of(), "prod-1", null, null, null, null);

        assertEquals(0L, count);
    }

    @Test
    void findLicenseAssignments_packageId_usesAggregationLookup() {
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("user_subpackages"), eq(UserSubPackage.class)))
                .thenReturn(new AggregationResults<>(List.of(), new Document()));

        repository.findLicenseAssignments(
                "client-1", null, null, null, "pkg-gold",
                null, null, null, 0, 10);

        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("user_subpackages"), eq(UserSubPackage.class));
    }

    @Test
    void findLicenseAssignments_expiringSoon_appliesExprCriteria() {
        when(mongoTemplate.find(any(Query.class), eq(UserSubPackage.class), eq("user_subpackages")))
                .thenReturn(List.of());

        repository.findLicenseAssignments(
                "client-1", null, null, null, null,
                "EXPIRING_SOON", null, null, 0, 10);

        verify(mongoTemplate).find(queryCaptor.capture(), eq(UserSubPackage.class), eq("user_subpackages"));
        Document queryObject = queryCaptor.getValue().getQueryObject();
        assertTrue(documentContainsKey(queryObject, "$expr"));
    }

    private static void assertEqualsLimit(Query query, int expectedLimit) {
        assertTrue(query.getLimit() == expectedLimit, "Expected limit " + expectedLimit + " but was " + query.getLimit());
    }

    private static boolean documentContainsKey(Document doc, String key) {
        if (doc.containsKey(key)) {
            return true;
        }
        for (Object value : doc.values()) {
            if (value instanceof Document nested && documentContainsKey(nested, key)) {
                return true;
            }
            if (value instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof Document nested && documentContainsKey(nested, key)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean documentContainsValue(Document doc, Object needle) {
        for (Object value : doc.values()) {
            if (needle.equals(value)) {
                return true;
            }
            if (value instanceof Document nested && documentContainsValue(nested, needle)) {
                return true;
            }
            if (value instanceof List<?> list) {
                if (list.contains(needle)) {
                    return true;
                }
                for (Object item : list) {
                    if (item instanceof Document nested && documentContainsValue(nested, needle)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
