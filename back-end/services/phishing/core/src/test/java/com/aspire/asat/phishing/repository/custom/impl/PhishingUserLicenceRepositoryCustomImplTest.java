package com.aspire.asat.phishing.repository.custom.impl;

import com.aspire.asat.phishing.dto.enums.RiskGroup;
import com.aspire.asat.phishing.dto.response.LicensedUserDepartmentCountDto;
import com.aspire.asat.phishing.dto.response.LicensedUserGroupCountDto;
import com.aspire.asat.phishing.model.PhishingUserLicence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.client.result.UpdateResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhishingUserLicenceRepositoryCustomImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private PhishingUserLicenceRepositoryCustomImpl repository;

    @Test
    void findLicensedUsers_blankPackage_returnsEmptyWithoutQuery() {
        List<PhishingUserLicence> result =
                repository.findLicensedUsers("client-1", "  ", null, null, null, 0, 10);

        assertTrue(result.isEmpty());
        verify(mongoTemplate, never()).find(any(Query.class), eq(PhishingUserLicence.class));
    }

    @Test
    void findLicensedUsers_appliesSkipAsOffsetTimesPageSize() {
        when(mongoTemplate.find(any(Query.class), eq(PhishingUserLicence.class)))
                .thenReturn(List.of());

        repository.findLicensedUsers("client-1", "pp-1", "ada", List.of("HR"), null, 2, 5);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(PhishingUserLicence.class));
        Query query = captor.getValue();
        assertEquals(10L, query.getSkip());
        assertEquals(5, query.getLimit());
        assertTrue(query.getQueryObject().toString().contains("active"));
    }

    @Test
    void findLicensedUsers_filtersActiveTrue() {
        when(mongoTemplate.find(any(Query.class), eq(PhishingUserLicence.class)))
                .thenReturn(List.of());

        repository.findLicensedUsers("client-1", "pp-1", null, null, null, 0, 10);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(PhishingUserLicence.class));
        String query = captor.getValue().getQueryObject().toString();
        assertTrue(query.contains("active"));
        assertTrue(query.contains("true"));
    }

    @Test
    void countLicensedUsers_blankClient_returnsZero() {
        assertEquals(0L, repository.countLicensedUsers(" ", "pp-1", null, null, null));
        verify(mongoTemplate, never()).count(any(Query.class), eq(PhishingUserLicence.class));
    }

    @Test
    void countLicensedUsers_delegatesToMongo() {
        when(mongoTemplate.count(any(Query.class), eq(PhishingUserLicence.class))).thenReturn(7L);

        long count = repository.countLicensedUsers("client-1", "pp-1", "bob", List.of("IT"), null);

        assertEquals(7L, count);
        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(captor.capture(), eq(PhishingUserLicence.class));
        assertTrue(captor.getValue().getQueryObject().toString().contains("active"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void countByDepartment_returnsMappedResults() {
        LicensedUserDepartmentCountDto row = LicensedUserDepartmentCountDto.builder()
                .departmentName("HR")
                .userCount(4)
                .build();
        AggregationResults<LicensedUserDepartmentCountDto> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(List.of(row));
        when(mongoTemplate.aggregate(
                any(Aggregation.class), eq("phishing_user_licence"), eq(LicensedUserDepartmentCountDto.class)))
                .thenReturn(results);

        List<LicensedUserDepartmentCountDto> counts =
                repository.countByDepartment("client-1", "pp-1");

        assertEquals(1, counts.size());
        assertEquals("HR", counts.get(0).getDepartmentName());
        assertEquals(4, counts.get(0).getUserCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    void countByGroup_returnsMappedResults() {
        LicensedUserGroupCountDto row = LicensedUserGroupCountDto.builder()
                .riskGroup(RiskGroup.HIGH_RISK)
                .userCount(2)
                .build();
        AggregationResults<LicensedUserGroupCountDto> results = mock(AggregationResults.class);
        when(results.getMappedResults()).thenReturn(List.of(row));
        when(mongoTemplate.aggregate(
                any(Aggregation.class), eq("phishing_user_licence"), eq(LicensedUserGroupCountDto.class)))
                .thenReturn(results);

        List<LicensedUserGroupCountDto> counts =
                repository.countByGroup("client-1", "pp-1");

        assertEquals(1, counts.size());
        assertEquals(RiskGroup.HIGH_RISK, counts.get(0).getRiskGroup());
        assertEquals(2, counts.get(0).getUserCount());
    }

    @Test
    void findLicensedUsers_appliesRiskGroupFilter() {
        when(mongoTemplate.find(any(Query.class), eq(PhishingUserLicence.class)))
                .thenReturn(List.of());

        List<RiskGroup> riskGroups = List.of(RiskGroup.HIGH_RISK, RiskGroup.CRITICAL_RISK);
        repository.findLicensedUsers("client-1", "pp-1", null, null, riskGroups, 0, 10);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(PhishingUserLicence.class));
        assertTrue(captor.getValue().getQueryObject().toString().contains("riskGroup"));
    }

    @Test
    void countByDepartment_blankPackage_returnsEmpty() {
        assertTrue(repository.countByDepartment("client-1", null).isEmpty());
        verify(mongoTemplate, never()).aggregate(any(Aggregation.class), any(String.class), any(Class.class));
    }

    @Test
    void findLicensedUserIds_doesNotFilterByActive() {
        when(mongoTemplate.find(any(Query.class), eq(PhishingUserLicence.class)))
                .thenReturn(List.of());

        repository.findLicensedUserIds("client-1", "pp-1");

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(PhishingUserLicence.class));
        assertTrue(!captor.getValue().getQueryObject().containsKey("active"));
    }

    @Test
    void findLicensedUserIds_returnsDistinctUserIds() {
        PhishingUserLicence row1 = PhishingUserLicence.builder()
                .userId("user-1")
                .clientAdminId("client-1")
                .productPackageId("pp-1")
                .build();
        PhishingUserLicence row2 = PhishingUserLicence.builder()
                .userId("user-1")
                .clientAdminId("client-1")
                .productPackageId("pp-1")
                .build();
        when(mongoTemplate.find(any(Query.class), eq(PhishingUserLicence.class)))
                .thenReturn(List.of(row1, row2));

        List<String> ids = repository.findLicensedUserIds("client-1", "pp-1");

        assertEquals(1, ids.size());
        assertEquals("user-1", ids.get(0));
    }

    @Test
    void findLicensedUserIds_blankClient_returnsEmptyWithoutQuery() {
        assertTrue(repository.findLicensedUserIds(" ", "pp-1").isEmpty());
        verify(mongoTemplate, never()).find(any(Query.class), eq(PhishingUserLicence.class));
    }

    @Test
    void updateUserSnapshot_appliesMatchAndSetFields() {
        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getMatchedCount()).thenReturn(2L);
        when(mongoTemplate.updateMulti(any(Query.class), any(Update.class), eq(PhishingUserLicence.class)))
                .thenReturn(updateResult);

        long matched = repository.updateUserSnapshot(
                "user-1", "client-1", "Ada", "Lovelace", "123", "HR", "Bangladesh", true);

        assertEquals(2L, matched);
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).updateMulti(
                queryCaptor.capture(), updateCaptor.capture(), eq(PhishingUserLicence.class));

        String query = queryCaptor.getValue().getQueryObject().toString();
        assertTrue(query.contains("userId"));
        assertTrue(query.contains("clientAdminId"));

        String update = updateCaptor.getValue().getUpdateObject().toString();
        assertTrue(update.contains("firstName"));
        assertTrue(update.contains("lastName"));
        assertTrue(update.contains("phoneNumber"));
        assertTrue(update.contains("departmentName"));
        assertTrue(update.contains("countryName"));
        assertTrue(update.contains("active"));
    }

    @Test
    void updateUserSnapshot_blankUserId_returnsZeroWithoutUpdate() {
        assertEquals(0L, repository.updateUserSnapshot(
                " ", "client-1", "A", "B", null, null, null, true));
        verify(mongoTemplate, never()).updateMulti(any(Query.class), any(Update.class), eq(PhishingUserLicence.class));
    }

    @Test
    void findAudienceLicences_blankPackage_returnsEmptyWithoutQuery() {
        assertTrue(repository.findAudienceLicences("client-1", " ", null, null, null).isEmpty());
        verify(mongoTemplate, never()).find(any(Query.class), eq(PhishingUserLicence.class));
    }

    @Test
    void findAudienceLicences_allUsers_filtersActiveOnly() {
        when(mongoTemplate.find(any(Query.class), eq(PhishingUserLicence.class))).thenReturn(List.of());

        repository.findAudienceLicences("client-1", "pp-1", null, null, null);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(PhishingUserLicence.class));
        String query = captor.getValue().getQueryObject().toString();
        assertTrue(query.contains("active"));
        assertTrue(query.contains("true"));
        assertTrue(query.contains("clientAdminId"));
        assertTrue(query.contains("productPackageId"));
    }

    @Test
    void findAudienceLicences_departments_matchIdOrName() {
        when(mongoTemplate.find(any(Query.class), eq(PhishingUserLicence.class))).thenReturn(List.of());

        repository.findAudienceLicences("client-1", "pp-1", List.of("HR", "dept-1"), null, null);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(PhishingUserLicence.class));
        String query = captor.getValue().getQueryObject().toString();
        assertTrue(query.contains("departmentId") || query.contains("departmentName"));
        assertTrue(query.contains("HR"));
    }

    @Test
    void findAudienceLicences_groups_matchGroupIdsOrRiskGroup() {
        when(mongoTemplate.find(any(Query.class), eq(PhishingUserLicence.class))).thenReturn(List.of());

        repository.findAudienceLicences(
                "client-1", "pp-1", null, List.of("g1", "HIGH_RISK"), null);

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(PhishingUserLicence.class));
        String query = captor.getValue().getQueryObject().toString();
        assertTrue(query.contains("groupIds"));
        assertTrue(query.contains("riskGroup") || query.contains("HIGH_RISK"));
    }

    @Test
    void findAudienceLicences_individual_filtersUserIds() {
        when(mongoTemplate.find(any(Query.class), eq(PhishingUserLicence.class))).thenReturn(List.of());

        repository.findAudienceLicences("client-1", "pp-1", null, null, List.of("u1", "u2"));

        ArgumentCaptor<Query> captor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).find(captor.capture(), eq(PhishingUserLicence.class));
        String query = captor.getValue().getQueryObject().toString();
        assertTrue(query.contains("userId"));
        assertTrue(query.contains("u1"));
    }
}
