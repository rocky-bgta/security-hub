package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.CmsPhishingCourseClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.client.RegistrationServiceClient.UserDto;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.CampaignTrainingData;
import com.aspire.asat.phishing.dto.cms.CmsPhishingCourseEnrollmentDto;
import com.aspire.asat.phishing.dto.cms.CmsPhishingCoursePageDto;
import com.aspire.asat.phishing.dto.cms.CmsPhishingCourseStatisticsCountsDto;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.response.PhishingCourseDetailDto;
import com.aspire.asat.phishing.dto.response.PhishingCourseStatisticsDto;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhishingCourseServiceImplTest {

    private static final String CLIENT_ADMIN_ID = "client-admin-1";

    @Mock
    private CmsPhishingCourseClient cmsPhishingCourseClient;
    @Mock
    private RegistrationServiceClient registrationServiceClient;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private UserCurrentContextService userCurrentContextService;

    @InjectMocks
    private PhishingCourseServiceImpl service;

    @BeforeEach
    void setUp() {
        CurrentUserContext context = CurrentUserContext.builder().clientAdminId(CLIENT_ADMIN_ID).build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
    }

    @Test
    void getStatistics_computesPercentagesUsingHalfUpRounding() {
        CmsPhishingCourseStatisticsCountsDto counts = CmsPhishingCourseStatisticsCountsDto.builder()
                .totalUsers(100)
                .completedUsers(80)
                .inProgressUsers(10)
                .pendingUsers(5)
                .expiredUsers(5)
                .build();
        when(campaignRepository.findTrainingModuleIdsByClientIdAndChannel(
                CLIENT_ADMIN_ID, CampaignChannel.EMAIL))
                .thenReturn(List.of("mod-email"));
        when(cmsPhishingCourseClient.getStatistics(eq(CLIENT_ADMIN_ID), any())).thenReturn(counts);

        PhishingCourseStatisticsDto result = service.getStatistics();

        assertEquals(100, result.getTotalUsers());
        assertEquals(80, result.getCompletedUsers());
        assertEquals(80, result.getCompletePercentage());
        assertEquals(10, result.getInProgressUsers());
        assertEquals(10, result.getInProgressPercentage());
        assertEquals(5, result.getPendingUsers());
        assertEquals(5, result.getPendingPercentage());
        assertEquals(5, result.getExpiredUsers());
        assertEquals(5, result.getExpiredPercentage());
    }

    @Test
    void getStatistics_returnsZeroPercentagesWhenTotalIsZero() {
        when(campaignRepository.findTrainingModuleIdsByClientIdAndChannel(
                CLIENT_ADMIN_ID, CampaignChannel.EMAIL))
                .thenReturn(List.of());

        PhishingCourseStatisticsDto result = service.getStatistics();

        assertEquals(0, result.getTotalUsers());
        assertEquals(0, result.getCompletePercentage());
        assertEquals(0, result.getInProgressPercentage());
        assertEquals(0, result.getPendingPercentage());
        assertEquals(0, result.getExpiredPercentage());
        verify(cmsPhishingCourseClient, never()).getStatistics(anyString(), any());
    }

    @Test
    void getStatistics_scopesCmsQueryToChannelTrainingModules() {
        CmsPhishingCourseStatisticsCountsDto counts = CmsPhishingCourseStatisticsCountsDto.builder()
                .totalUsers(10)
                .completedUsers(4)
                .inProgressUsers(3)
                .pendingUsers(2)
                .expiredUsers(1)
                .build();
        when(campaignRepository.findTrainingModuleIdsByClientIdAndChannel(
                CLIENT_ADMIN_ID, CampaignChannel.SMS))
                .thenReturn(List.of("sms-mod"));
        when(cmsPhishingCourseClient.getStatistics(eq(CLIENT_ADMIN_ID), any())).thenReturn(counts);

        PhishingCourseStatisticsDto result = service.getStatistics(CampaignChannel.SMS);

        assertEquals(10, result.getTotalUsers());
        assertEquals(4, result.getCompletedUsers());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<String>> moduleIdsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(cmsPhishingCourseClient).getStatistics(eq(CLIENT_ADMIN_ID), moduleIdsCaptor.capture());
        assertEquals(Set.of("sms-mod"), Set.copyOf(moduleIdsCaptor.getValue()));
        verify(campaignRepository, never()).findTrainingModuleIdsByClientIdAndChannel(
                CLIENT_ADMIN_ID, CampaignChannel.EMAIL);
    }

    @Test
    void getDetails_resolvesEmailToUserIdsAndForwardsToCms() {
        UserDto matchingUser = UserDto.builder()
                .userId("user-1")
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .departmentName("Engineering")
                .build();
        UserDto otherUser = UserDto.builder()
                .userId("user-2")
                .firstName("Bob")
                .lastName("Smith")
                .email("bob@example.com")
                .departmentName("Sales")
                .build();
        when(registrationServiceClient.getAllUsers(CLIENT_ADMIN_ID)).thenReturn(List.of(matchingUser, otherUser));

        CmsPhishingCourseEnrollmentDto enrollment = CmsPhishingCourseEnrollmentDto.builder()
                .userId("user-1")
                .subPackageId("sp-1")
                .subPackageName("Phishing 101")
                .assignedDate(LocalDate.of(2024, 6, 19))
                .expiryDate(LocalDate.of(2024, 7, 19))
                .status("InProgress")
                .progress(45.0)
                .build();
        CmsPhishingCoursePageDto page = CmsPhishingCoursePageDto.builder()
                .offset(0).pageSize(10).total(1L).items(List.of(enrollment)).build();
        when(cmsPhishingCourseClient.getDetails(eq(CLIENT_ADMIN_ID), eq(0), eq(10), any()))
                .thenReturn(page);

        Campaign campaign = Campaign.builder()
                .id("camp-1")
                .clientId(CLIENT_ADMIN_ID)
                .campaignName("Q2 Phishing Drill")
                .trainingData(CampaignTrainingData.builder().trainingModuleId("sp-1").build())
                .build();
        when(campaignRepository.findByClientIdAndTrainingModuleIdIn(eq(CLIENT_ADMIN_ID), any()))
                .thenReturn(List.of(campaign));

        AllResponseDto<List<PhishingCourseDetailDto>> result = service.getDetails(0, 10, "JANE");

        assertEquals(0, result.getOffset());
        assertEquals(10, result.getPageSize());
        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getItems().size());

        PhishingCourseDetailDto detail = result.getItems().get(0);
        assertEquals("Q2 Phishing Drill", detail.getCampaignName());
        assertEquals("Jane Doe", detail.getUserName());
        assertEquals("jane@example.com", detail.getEmail());
        assertEquals("Engineering", detail.getDepartment());
        assertEquals("InProgress", detail.getStatus());
        assertEquals(45.0, detail.getProgress());

        assertEquals(LocalDate.of(2024, 6, 19), detail.getAssignedDate());
        assertEquals(LocalDate.of(2024, 7, 19), detail.getExpireDate());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<String>> userIdsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(cmsPhishingCourseClient).getDetails(eq(CLIENT_ADMIN_ID), eq(0), eq(10), userIdsCaptor.capture());
        assertEquals(Set.of("user-1"), Set.copyOf(userIdsCaptor.getValue()));

        verify(registrationServiceClient, org.mockito.Mockito.never()).getUsersByIds(anyString(), any());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<String>> moduleIdsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(campaignRepository).findByClientIdAndTrainingModuleIdIn(eq(CLIENT_ADMIN_ID), moduleIdsCaptor.capture());
        assertEquals(Set.of("sp-1"), Set.copyOf(moduleIdsCaptor.getValue()));
    }

    @Test
    void getDetails_shortCircuitsWithEmptyPageWhenEmailMatchesNoUsers() {
        UserDto otherUser = UserDto.builder()
                .userId("user-2")
                .email("bob@example.com")
                .build();
        when(registrationServiceClient.getAllUsers(CLIENT_ADMIN_ID)).thenReturn(List.of(otherUser));

        AllResponseDto<List<PhishingCourseDetailDto>> result = service.getDetails(0, 10, "nobody");

        assertEquals(0, result.getOffset());
        assertEquals(10, result.getPageSize());
        assertEquals(0L, result.getTotal());
        assertEquals(0, result.getItems().size());

        verify(cmsPhishingCourseClient, org.mockito.Mockito.never()).getDetails(anyString(), anyInt(), anyInt(), any());
        verify(registrationServiceClient, org.mockito.Mockito.never()).getUsersByIds(anyString(), any());
        verify(campaignRepository, org.mockito.Mockito.never()).findByClientIdAndTrainingModuleIdIn(anyString(), any());
    }

    @Test
    void getDetails_fallsBackToSubPackageNameWhenCampaignMissing() {
        CmsPhishingCourseEnrollmentDto enrollment = CmsPhishingCourseEnrollmentDto.builder()
                .userId("user-2")
                .subPackageId("sp-unknown")
                .subPackageName("Standalone Course")
                .assignedDate(LocalDate.of(2024, 1, 1))
                .status("pending")
                .progress(0.0)
                .build();
        CmsPhishingCoursePageDto page = CmsPhishingCoursePageDto.builder()
                .offset(0).pageSize(10).total(1L).items(List.of(enrollment)).build();
        when(cmsPhishingCourseClient.getDetails(eq(CLIENT_ADMIN_ID), anyInt(), anyInt(), any()))
                .thenReturn(page);
        when(registrationServiceClient.getUsersByIds(eq(CLIENT_ADMIN_ID), any())).thenReturn(List.of());
        when(campaignRepository.findByClientIdAndTrainingModuleIdIn(eq(CLIENT_ADMIN_ID), any())).thenReturn(List.of());

        AllResponseDto<List<PhishingCourseDetailDto>> result = service.getDetails(0, 10, null);

        assertEquals(1, result.getItems().size());
        PhishingCourseDetailDto detail = result.getItems().get(0);
        assertEquals("Standalone Course", detail.getCampaignName());
        assertNull(detail.getUserName());
        assertNull(detail.getEmail());
        assertNull(detail.getDepartment());
        assertEquals("pending", detail.getStatus());
        assertNull(detail.getExpireDate());
        assertNotNull(detail.getAssignedDate());
    }

    @Test
    void getDetails_returnsEmptyPageWithoutCallingDownstreamsWhenNoEnrollments() {
        CmsPhishingCoursePageDto page = CmsPhishingCoursePageDto.builder()
                .offset(2).pageSize(5).total(0L).items(List.of()).build();
        when(cmsPhishingCourseClient.getDetails(eq(CLIENT_ADMIN_ID), eq(2), eq(5), any()))
                .thenReturn(page);

        AllResponseDto<List<PhishingCourseDetailDto>> result = service.getDetails(2, 5, "  ");

        assertEquals(2, result.getOffset());
        assertEquals(5, result.getPageSize());
        assertEquals(0L, result.getTotal());
        assertEquals(0, result.getItems().size());

        verify(registrationServiceClient, org.mockito.Mockito.never()).getUsersByIds(anyString(), any());
        verify(campaignRepository, org.mockito.Mockito.never()).findByClientIdAndTrainingModuleIdIn(anyString(), any());
    }
}
