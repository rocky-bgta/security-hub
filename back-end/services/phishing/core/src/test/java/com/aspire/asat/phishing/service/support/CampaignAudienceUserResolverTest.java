package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient.UserDto;
import com.aspire.asat.phishing.dto.enums.AudienceType;
import com.aspire.asat.phishing.dto.enums.RiskGroup;
import com.aspire.asat.phishing.dto.request.CampaignAudienceRequest;
import com.aspire.asat.phishing.model.PhishingUserLicence;
import com.aspire.asat.phishing.repository.PhishingUserLicenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignAudienceUserResolverTest {

    private static final String CLIENT_ID = "client-1";
    private static final String PRODUCT_PACKAGE_ID = "pp-1";

    @Mock private RegistrationServiceClient registrationClient;
    @Mock private PhishingUserLicenceRepository phishingUserLicenceRepository;

    @InjectMocks
    private CampaignAudienceUserResolver resolver;

    @Test
    void resolve_allUsers_usesRegistration() {
        when(registrationClient.getAllUsers(CLIENT_ID)).thenReturn(List.of(regUser("u1")));

        List<UserDto> result = resolver.resolve(CLIENT_ID, request(AudienceType.ALL_USERS, null, null, null));

        assertEquals(1, result.size());
        assertEquals("u1", result.get(0).getUserId());
        verify(phishingUserLicenceRepository, never()).findAudienceLicences(
                any(), any(), any(), any(), any());
    }

    @Test
    void resolveLicensed_allUsers_queriesLicencesWithoutFilters() {
        when(phishingUserLicenceRepository.findAudienceLicences(
                CLIENT_ID, PRODUCT_PACKAGE_ID, null, null, null))
                .thenReturn(List.of(licence("u1", true), licence("u2", true)));

        List<UserDto> result = resolver.resolveLicensed(
                CLIENT_ID, PRODUCT_PACKAGE_ID, request(AudienceType.ALL_USERS, null, null, null));

        assertEquals(2, result.size());
        assertEquals("u1", result.get(0).getUserId());
        assertEquals("Ada", result.get(0).getFirstName());
        verify(registrationClient, never()).getAllUsers(any());
    }

    @Test
    void resolveLicensed_departments_passesDepartmentIds() {
        when(phishingUserLicenceRepository.findAudienceLicences(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), eq(List.of("HR")), isNull(), isNull()))
                .thenReturn(List.of(licence("u1", true)));

        List<UserDto> result = resolver.resolveLicensed(
                CLIENT_ID, PRODUCT_PACKAGE_ID,
                request(AudienceType.DEPARTMENTS, List.of("HR"), null, null));

        assertEquals(1, result.size());
        verify(phishingUserLicenceRepository).findAudienceLicences(
                CLIENT_ID, PRODUCT_PACKAGE_ID, List.of("HR"), null, null);
    }

    @Test
    void resolveLicensed_departments_emptyList_returnsEmpty() {
        List<UserDto> result = resolver.resolveLicensed(
                CLIENT_ID, PRODUCT_PACKAGE_ID,
                request(AudienceType.DEPARTMENTS, List.of(), null, null));

        assertTrue(result.isEmpty());
        verify(phishingUserLicenceRepository, never()).findAudienceLicences(
                any(), any(), any(), any(), any());
    }

    @Test
    void resolveLicensed_groups_passesGroupIdsIncludingRiskChip() {
        when(phishingUserLicenceRepository.findAudienceLicences(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), isNull(),
                eq(List.of("g1", "HIGH_RISK")), isNull()))
                .thenReturn(List.of(licence("u1", true)));

        List<UserDto> result = resolver.resolveLicensed(
                CLIENT_ID, PRODUCT_PACKAGE_ID,
                request(AudienceType.GROUPS, null, List.of("g1", "HIGH_RISK"), null));

        assertEquals(1, result.size());
        verify(phishingUserLicenceRepository).findAudienceLicences(
                CLIENT_ID, PRODUCT_PACKAGE_ID, null, List.of("g1", "HIGH_RISK"), null);
    }

    @Test
    void resolveLicensed_individual_passesUserIds() {
        when(phishingUserLicenceRepository.findAudienceLicences(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), isNull(), isNull(), eq(List.of("u1", "u9"))))
                .thenReturn(List.of(licence("u1", true)));

        List<UserDto> result = resolver.resolveLicensed(
                CLIENT_ID, PRODUCT_PACKAGE_ID,
                request(AudienceType.INDIVIDUAL, null, null, List.of("u1", "u9")));

        assertEquals(1, result.size());
        assertEquals("u1", result.get(0).getUserId());
    }

    @Test
    void resolveLicensed_blankProductPackage_returnsEmpty() {
        assertTrue(resolver.resolveLicensed(
                CLIENT_ID, "  ", request(AudienceType.ALL_USERS, null, null, null)).isEmpty());
        verify(phishingUserLicenceRepository, never()).findAudienceLicences(
                any(), any(), any(), any(), any());
    }

    @Test
    void resolveLicensed_mapsRiskGroupAndDepartment() {
        PhishingUserLicence row = licence("u1", true);
        row.setRiskGroup(RiskGroup.HIGH_RISK);
        row.setDepartmentName("Engineering");
        when(phishingUserLicenceRepository.findAudienceLicences(
                CLIENT_ID, PRODUCT_PACKAGE_ID, null, null, null))
                .thenReturn(List.of(row));

        UserDto user = resolver.resolveLicensed(
                CLIENT_ID, PRODUCT_PACKAGE_ID, request(AudienceType.ALL_USERS, null, null, null)).get(0);

        assertEquals(RiskGroup.HIGH_RISK, user.getRiskGroup());
        assertEquals("Engineering", user.getDepartmentName());
    }

    private static CampaignAudienceRequest request(
            AudienceType type, List<String> departments, List<String> groups, List<String> userIds) {
        return CampaignAudienceRequest.builder()
                .audienceType(type)
                .departmentIds(departments != null ? departments : List.of())
                .groupIds(groups != null ? groups : List.of())
                .userIds(userIds != null ? userIds : List.of())
                .build();
    }

    private static UserDto regUser(String id) {
        return UserDto.builder().userId(id).email(id + "@ex.com").active(true).build();
    }

    private static PhishingUserLicence licence(String userId, boolean active) {
        return PhishingUserLicence.builder()
                .userId(userId)
                .email(userId + "@ex.com")
                .firstName("Ada")
                .lastName("Lovelace")
                .departmentId("dept-1")
                .departmentName("Engineering")
                .organizationName("Org")
                .organizationDomain("org.com")
                .phoneNumber("123")
                .countryName("US")
                .groupIds(List.of("g1"))
                .active(active)
                .isRiskProfileExist(true)
                .riskGroup(RiskGroup.MEDIUM_RISK)
                .build();
    }
}
