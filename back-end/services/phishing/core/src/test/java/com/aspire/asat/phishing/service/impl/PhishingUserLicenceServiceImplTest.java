package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient.ClientProductDetailDto;
import com.aspire.asat.phishing.client.RegistrationServiceClient.UserDto;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.enums.AudienceType;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.RiskGroup;
import com.aspire.asat.phishing.dto.request.AllocateLicenceRequest;
import com.aspire.asat.phishing.dto.response.AllocateLicenceResponseDto;
import com.aspire.asat.phishing.dto.response.LicensedUserDepartmentCountDto;
import com.aspire.asat.phishing.dto.response.LicensedUserDto;
import com.aspire.asat.phishing.dto.response.LicensedUserGroupCountDto;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.PhishingUserLicence;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.PhishingUserLicenceRepository;
import com.aspire.asat.phishing.service.support.CampaignAudienceUserResolver;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhishingUserLicenceServiceImplTest {

    private static final String CLIENT_ID = "client-1";
    private static final String CAMPAIGN_ID = "cmp-1";
    private static final String PRODUCT_PACKAGE_ID = "pp-1";

    @Mock private CampaignRepository campaignRepository;
    @Mock private PhishingUserLicenceRepository phishingUserLicenceRepository;
    @Mock private CampaignAudienceUserResolver campaignAudienceUserResolver;
    @Mock private RegistrationServiceClient registrationServiceClient;
    @Mock private UserCurrentContextService userCurrentContextService;

    @InjectMocks
    private PhishingUserLicenceServiceImpl service;

    @BeforeEach
    void setUp() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());
        when(campaignRepository.findByIdAndClientId(CAMPAIGN_ID, CLIENT_ID))
                .thenReturn(Optional.of(draftCampaign()));
        org.mockito.Mockito.lenient()
                .when(registrationServiceClient.getProductPackageDetail(PRODUCT_PACKAGE_ID))
                .thenReturn(activeProduct(200));
    }

    @Test
    void allocate_campaign1_allNew_inserts100() {
        List<UserDto> users = users(1, 100);
        stubAudience(users);
        when(phishingUserLicenceRepository.findByClientAdminIdAndProductPackageIdAndUserIdIn(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), anyCollection()))
                .thenReturn(List.of());
        when(phishingUserLicenceRepository.countByClientAdminIdAndProductPackageId(CLIENT_ID, PRODUCT_PACKAGE_ID))
                .thenReturn(0L);

        AllocateLicenceResponseDto result = service.allocateLicence(CAMPAIGN_ID, confirmRequest(true));

        assertEquals(100, result.getNewLicenseAllocatedCount());
        assertEquals(100, result.getUsedLicenseCount());
        assertEquals(100, result.getAvailableLicenseCount());
        assertEquals(100, result.getUserIds().size());
        assertFalse(result.isRequiresConfirmation());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PhishingUserLicence>> captor = ArgumentCaptor.forClass(List.class);
        verify(phishingUserLicenceRepository).saveAll(captor.capture());
        assertEquals(100, captor.getValue().size());
        verify(registrationServiceClient).updateClientProductUsedLicenseCount(PRODUCT_PACKAGE_ID, 100);
    }

    @Test
    void allocate_persistsRiskGroupFromUserDto() {
        UserDto user = UserDto.builder()
                .userId("user-1")
                .email("user1@example.com")
                .firstName("Ada")
                .lastName("Lovelace")
                .active(true)
                .riskGroup(RiskGroup.HIGH_RISK)
                .build();
        stubAudience(List.of(user));
        when(phishingUserLicenceRepository.findByClientAdminIdAndProductPackageIdAndUserIdIn(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), anyCollection()))
                .thenReturn(List.of());
        when(phishingUserLicenceRepository.countByClientAdminIdAndProductPackageId(CLIENT_ID, PRODUCT_PACKAGE_ID))
                .thenReturn(0L);

        service.allocateLicence(CAMPAIGN_ID, confirmRequest(true));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PhishingUserLicence>> captor = ArgumentCaptor.forClass(List.class);
        verify(phishingUserLicenceRepository).saveAll(captor.capture());
        assertEquals(RiskGroup.HIGH_RISK, captor.getValue().get(0).getRiskGroup());
    }

    @Test
    void allocate_campaign2_50existing100new_inserts100() {
        List<UserDto> users = users(1, 150);
        stubAudience(users);
        List<PhishingUserLicence> existing = users.subList(0, 50).stream()
                .map(u -> PhishingUserLicence.builder().userId(u.getUserId()).build())
                .collect(Collectors.toList());
        when(phishingUserLicenceRepository.findByClientAdminIdAndProductPackageIdAndUserIdIn(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), anyCollection()))
                .thenReturn(existing);
        when(phishingUserLicenceRepository.countByClientAdminIdAndProductPackageId(CLIENT_ID, PRODUCT_PACKAGE_ID))
                .thenReturn(100L);

        AllocateLicenceResponseDto result = service.allocateLicence(CAMPAIGN_ID, confirmRequest(true));

        assertEquals(50, result.getExistingLicensedUserCount());
        assertEquals(100, result.getNewLicenseRequiredCount());
        assertEquals(100, result.getNewLicenseAllocatedCount());
        assertEquals(200, result.getUsedLicenseCount());
        assertEquals(0, result.getAvailableLicenseCount());
        assertEquals(150, result.getUserIds().size());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PhishingUserLicence>> captor = ArgumentCaptor.forClass(List.class);
        verify(phishingUserLicenceRepository).saveAll(captor.capture());
        assertEquals(100, captor.getValue().size());
        verify(registrationServiceClient).updateClientProductUsedLicenseCount(PRODUCT_PACKAGE_ID, 200);
    }

    @Test
    void allocate_campaign3_allExisting_insertsNothing() {
        List<UserDto> users = users(1, 200);
        stubAudience(users);
        List<PhishingUserLicence> existing = users.stream()
                .map(u -> PhishingUserLicence.builder().userId(u.getUserId()).build())
                .collect(Collectors.toList());
        when(phishingUserLicenceRepository.findByClientAdminIdAndProductPackageIdAndUserIdIn(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), anyCollection()))
                .thenReturn(existing);
        when(phishingUserLicenceRepository.countByClientAdminIdAndProductPackageId(CLIENT_ID, PRODUCT_PACKAGE_ID))
                .thenReturn(200L);

        AllocateLicenceResponseDto result = service.allocateLicence(CAMPAIGN_ID, confirmRequest(true));

        assertEquals(0, result.getNewLicenseAllocatedCount());
        assertEquals(200, result.getUsedLicenseCount());
        assertEquals(0, result.getAvailableLicenseCount());
        assertEquals(200, result.getUserIds().size());
        verify(phishingUserLicenceRepository, never()).saveAll(any());
        verify(registrationServiceClient).updateClientProductUsedLicenseCount(PRODUCT_PACKAGE_ID, 200);
    }

    @Test
    void allocate_overLimit_confirmFalse_noInsert_requiresConfirmation() {
        List<UserDto> users = users(1, 220);
        stubAudience(users);
        when(phishingUserLicenceRepository.findByClientAdminIdAndProductPackageIdAndUserIdIn(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), anyCollection()))
                .thenReturn(List.of());
        when(phishingUserLicenceRepository.countByClientAdminIdAndProductPackageId(CLIENT_ID, PRODUCT_PACKAGE_ID))
                .thenReturn(0L);

        AllocateLicenceResponseDto result = service.allocateLicence(CAMPAIGN_ID, confirmRequest(false));

        assertTrue(result.isRequiresConfirmation());
        assertEquals(0, result.getNewLicenseAllocatedCount());
        assertEquals(0, result.getUsedLicenseCount());
        assertEquals(200, result.getAvailableLicenseCount());
        assertEquals(220, result.getNewLicenseRequiredCount());
        assertEquals(
                "License Limit Reached\n"
                        + "You selected 220 users, but only 200 licenses are available.\n"
                        + "The campaign will be sent to the first 200 users in your selected list. "
                        + "The remaining 20 users will be skipped.",
                result.getConfirmationMessage());
        assertEquals(200, result.getUserIds().size());
        verify(phishingUserLicenceRepository, never()).saveAll(any());
        verify(registrationServiceClient, never()).updateClientProductUsedLicenseCount(anyString(), anyInt());
    }

    @Test
    void allocate_secondCampaign_partialSeats_confirmFalse_usesRemainingInMessage() {
        when(registrationServiceClient.getProductPackageDetail(PRODUCT_PACKAGE_ID))
                .thenReturn(activeProduct(15));
        List<UserDto> users = users(11, 10);
        stubAudience(users);
        when(phishingUserLicenceRepository.findByClientAdminIdAndProductPackageIdAndUserIdIn(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), anyCollection()))
                .thenReturn(List.of());
        when(phishingUserLicenceRepository.countByClientAdminIdAndProductPackageId(CLIENT_ID, PRODUCT_PACKAGE_ID))
                .thenReturn(10L);

        AllocateLicenceResponseDto result = service.allocateLicence(CAMPAIGN_ID, confirmRequest(false));

        assertTrue(result.isRequiresConfirmation());
        assertEquals(15, result.getLicenseCount());
        assertEquals(10, result.getUsedLicenseCount());
        assertEquals(5, result.getAvailableLicenseCount());
        assertEquals(10, result.getNewLicenseRequiredCount());
        assertEquals(0, result.getNewLicenseAllocatedCount());
        assertEquals(5, result.getUserIds().size());
        assertEquals(
                "License Limit Reached\n"
                        + "You selected 10 users, but only 5 licenses are available.\n"
                        + "The campaign will be sent to the first 5 users in your selected list. "
                        + "The remaining 5 users will be skipped.",
                result.getConfirmationMessage());
        verify(phishingUserLicenceRepository, never()).saveAll(any());
        verify(registrationServiceClient, never()).updateClientProductUsedLicenseCount(anyString(), anyInt());
    }

    @Test
    void allocate_overLimit_confirmTrue_insertsRemainingOnly() {
        List<UserDto> users = users(1, 220);
        stubAudience(users);
        when(phishingUserLicenceRepository.findByClientAdminIdAndProductPackageIdAndUserIdIn(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), anyCollection()))
                .thenReturn(List.of());
        when(phishingUserLicenceRepository.countByClientAdminIdAndProductPackageId(CLIENT_ID, PRODUCT_PACKAGE_ID))
                .thenReturn(0L);

        AllocateLicenceResponseDto result = service.allocateLicence(CAMPAIGN_ID, confirmRequest(true));

        assertFalse(result.isRequiresConfirmation());
        assertEquals(200, result.getNewLicenseAllocatedCount());
        assertEquals(200, result.getUsedLicenseCount());
        assertEquals(0, result.getAvailableLicenseCount());
        assertEquals(200, result.getUserIds().size());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PhishingUserLicence>> captor = ArgumentCaptor.forClass(List.class);
        verify(phishingUserLicenceRepository).saveAll(captor.capture());
        assertEquals(200, captor.getValue().size());
        verify(registrationServiceClient).updateClientProductUsedLicenseCount(PRODUCT_PACKAGE_ID, 200);
    }

    @Test
    void allocate_registrationSyncFails_throwsServiceUnavailable() {
        List<UserDto> users = users(1, 10);
        stubAudience(users);
        when(phishingUserLicenceRepository.findByClientAdminIdAndProductPackageIdAndUserIdIn(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), anyCollection()))
                .thenReturn(List.of());
        when(phishingUserLicenceRepository.countByClientAdminIdAndProductPackageId(CLIENT_ID, PRODUCT_PACKAGE_ID))
                .thenReturn(0L);
        doThrow(new RuntimeException("registration down"))
                .when(registrationServiceClient)
                .updateClientProductUsedLicenseCount(PRODUCT_PACKAGE_ID, 10);

        ServiceException ex = assertThrows(ServiceException.class,
                () -> service.allocateLicence(CAMPAIGN_ID, confirmRequest(true)));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatus());
        verify(phishingUserLicenceRepository).saveAll(any());
    }

    @Test
    void allocate_missingProductPackageId_throws() {
        Campaign campaign = draftCampaign();
        campaign.setProductPackageId(null);
        when(campaignRepository.findByIdAndClientId(CAMPAIGN_ID, CLIENT_ID))
                .thenReturn(Optional.of(campaign));

        assertThrows(PhishingValidationException.class,
                () -> service.allocateLicence(CAMPAIGN_ID, confirmRequest(true)));
    }

    @Test
    void allocate_inactivePackage_throws() {
        when(registrationServiceClient.getProductPackageDetail(PRODUCT_PACKAGE_ID))
                .thenReturn(ClientProductDetailDto.builder()
                        .id(PRODUCT_PACKAGE_ID)
                        .clientAdminId(CLIENT_ID)
                        .licenseCount(200)
                        .licenseStatus("PENDING")
                        .expiryDate(Instant.now().plus(30, ChronoUnit.DAYS))
                        .build());

        assertThrows(PhishingValidationException.class,
                () -> service.allocateLicence(CAMPAIGN_ID, confirmRequest(true)));
    }

    @Test
    void allocate_emptyAudience_throws() {
        stubAudience(List.of());

        assertThrows(PhishingValidationException.class,
                () -> service.allocateLicence(CAMPAIGN_ID, confirmRequest(true)));
    }

    @Test
    void listLicensedUsers_emptyPackage_returnsEmptyPage() {
        when(phishingUserLicenceRepository.findLicensedUsers(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(List.of());
        when(phishingUserLicenceRepository.countLicensedUsers(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), isNull(), isNull(), isNull()))
                .thenReturn(0L);

        AllResponseDto<List<LicensedUserDto>> result =
                service.listLicensedUsers(CAMPAIGN_ID, null, null, null, 0, 10);

        assertEquals(0L, result.getTotal());
        assertTrue(result.getItems().isEmpty());
        assertEquals(0, result.getOffset());
        assertEquals(10, result.getPageSize());
    }

    @Test
    void listLicensedUsers_paginationUsesPageIndex() {
        PhishingUserLicence row = PhishingUserLicence.builder()
                .userId("user-11")
                .email("user11@example.com")
                .firstName("Ada")
                .lastName("Lovelace")
                .departmentName("Engineering")
                .clientAdminId(CLIENT_ID)
                .active(true)
                .isRiskProfileExist(false)
                .riskGroup(RiskGroup.MEDIUM_RISK)
                .build();
        when(phishingUserLicenceRepository.findLicensedUsers(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), isNull(), isNull(), isNull(), eq(2), eq(5)))
                .thenReturn(List.of(row));
        when(phishingUserLicenceRepository.countLicensedUsers(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), isNull(), isNull(), isNull()))
                .thenReturn(12L);

        AllResponseDto<List<LicensedUserDto>> result =
                service.listLicensedUsers(CAMPAIGN_ID, null, null, null, 2, 5);

        assertEquals(12L, result.getTotal());
        assertEquals(2, result.getOffset());
        assertEquals(5, result.getPageSize());
        assertEquals(1, result.getItems().size());
        LicensedUserDto dto = result.getItems().get(0);
        assertEquals("user-11", dto.getId());
        assertEquals("Ada Lovelace", dto.getFullName());
        assertEquals("Engineering", dto.getDepartment());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals(RiskGroup.MEDIUM_RISK, dto.getRiskGroup());

        verify(phishingUserLicenceRepository).findLicensedUsers(
                CLIENT_ID, PRODUCT_PACKAGE_ID, null, null, null, 2, 5);
    }

    @Test
    void listLicensedUsers_passesSearchDepartmentsAndRiskGroups() {
        List<String> departments = List.of("HR", "IT");
        List<RiskGroup> riskGroups = List.of(RiskGroup.HIGH_RISK, RiskGroup.LOW_RISK);
        when(phishingUserLicenceRepository.findLicensedUsers(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), eq("alice"), eq(departments), eq(riskGroups), eq(0), eq(10)))
                .thenReturn(List.of());
        when(phishingUserLicenceRepository.countLicensedUsers(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), eq("alice"), eq(departments), eq(riskGroups)))
                .thenReturn(0L);

        service.listLicensedUsers(CAMPAIGN_ID, "alice", departments, riskGroups, 0, 10);

        verify(phishingUserLicenceRepository).findLicensedUsers(
                CLIENT_ID, PRODUCT_PACKAGE_ID, "alice", departments, riskGroups, 0, 10);
        verify(phishingUserLicenceRepository).countLicensedUsers(
                CLIENT_ID, PRODUCT_PACKAGE_ID, "alice", departments, riskGroups);
    }

    @Test
    void listLicensedUsers_passesSearchAndDepartments() {
        List<String> departments = List.of("HR", "IT");
        when(phishingUserLicenceRepository.findLicensedUsers(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), eq("alice"), eq(departments), isNull(), eq(0), eq(10)))
                .thenReturn(List.of());
        when(phishingUserLicenceRepository.countLicensedUsers(
                eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID), eq("alice"), eq(departments), isNull()))
                .thenReturn(0L);

        service.listLicensedUsers(CAMPAIGN_ID, "alice", departments, null, 0, 10);

        verify(phishingUserLicenceRepository).findLicensedUsers(
                CLIENT_ID, PRODUCT_PACKAGE_ID, "alice", departments, null, 0, 10);
        verify(phishingUserLicenceRepository).countLicensedUsers(
                CLIENT_ID, PRODUCT_PACKAGE_ID, "alice", departments, null);
    }

    @Test
    void listLicensedUsers_inactiveMapsToInactiveStatus() {
        PhishingUserLicence row = PhishingUserLicence.builder()
                .userId("user-x")
                .email("x@example.com")
                .firstName("X")
                .active(false)
                .clientAdminId(CLIENT_ID)
                .build();
        when(phishingUserLicenceRepository.findLicensedUsers(
                anyString(), anyString(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(row));
        when(phishingUserLicenceRepository.countLicensedUsers(anyString(), anyString(), any(), any(), any()))
                .thenReturn(1L);

        AllResponseDto<List<LicensedUserDto>> result =
                service.listLicensedUsers(CAMPAIGN_ID, null, null, null, 0, 10);

        assertEquals("INACTIVE", result.getItems().get(0).getStatus());
    }

    @Test
    void getLicensedUserDepartmentCounts_delegatesToRepo() {
        List<LicensedUserDepartmentCountDto> counts = List.of(
                LicensedUserDepartmentCountDto.builder().departmentName("HR").userCount(3).build());
        when(phishingUserLicenceRepository.countByDepartment(CLIENT_ID, PRODUCT_PACKAGE_ID))
                .thenReturn(counts);

        assertEquals(counts, service.getLicensedUserDepartmentCounts(CAMPAIGN_ID));
    }

    @Test
    void getLicensedUserGroupCounts_delegatesToRepo() {
        List<LicensedUserGroupCountDto> counts = List.of(
                LicensedUserGroupCountDto.builder().riskGroup(RiskGroup.HIGH_RISK).userCount(2).build());
        when(phishingUserLicenceRepository.countByGroup(CLIENT_ID, PRODUCT_PACKAGE_ID))
                .thenReturn(counts);

        assertEquals(counts, service.getLicensedUserGroupCounts(CAMPAIGN_ID));
    }

    @Test
    void listLicensedUsers_missingCampaign_throws() {
        when(campaignRepository.findByIdAndClientId(CAMPAIGN_ID, CLIENT_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.listLicensedUsers(CAMPAIGN_ID, null, null, null, 0, 10));
        verify(phishingUserLicenceRepository, never())
                .findLicensedUsers(anyString(), anyString(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void listLicensedUsers_missingProductPackageId_throws() {
        Campaign campaign = draftCampaign();
        campaign.setProductPackageId(null);
        when(campaignRepository.findByIdAndClientId(CAMPAIGN_ID, CLIENT_ID))
                .thenReturn(Optional.of(campaign));

        assertThrows(PhishingValidationException.class,
                () -> service.listLicensedUsers(CAMPAIGN_ID, null, null, null, 0, 10));
        verify(phishingUserLicenceRepository, never())
                .findLicensedUsers(anyString(), anyString(), any(), any(), any(), anyInt(), anyInt());
    }

    private void stubAudience(List<UserDto> users) {
        when(campaignAudienceUserResolver.resolve(eq(CLIENT_ID), any())).thenReturn(users);
    }

    private static AllocateLicenceRequest confirmRequest(boolean confirm) {
        return AllocateLicenceRequest.builder()
                .audienceType(AudienceType.INDIVIDUAL)
                .userIds(List.of("u-placeholder"))
                .confirm(confirm)
                .build();
    }

    private static Campaign draftCampaign() {
        return Campaign.builder()
                .id(CAMPAIGN_ID)
                .clientId(CLIENT_ID)
                .campaignName("Test")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .status(CampaignStatus.DRAFT)
                .productPackageId(PRODUCT_PACKAGE_ID)
                .build();
    }

    private static ClientProductDetailDto activeProduct(int licenseCount) {
        return ClientProductDetailDto.builder()
                .id(PRODUCT_PACKAGE_ID)
                .clientAdminId(CLIENT_ID)
                .licenseCount(licenseCount)
                .licenseStatus("ACTIVE")
                .expiryDate(Instant.now().plus(365, ChronoUnit.DAYS))
                .build();
    }

    private static List<UserDto> users(int from, int count) {
        List<UserDto> list = new ArrayList<>();
        for (int i = from; i < from + count; i++) {
            list.add(UserDto.builder()
                    .userId("user-" + i)
                    .email("user" + i + "@example.com")
                    .firstName("F" + i)
                    .lastName("L" + i)
                    .active(true)
                    .build());
        }
        return list;
    }
}
