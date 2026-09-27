package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.client.responseDto.PhishingCourseEnrollmentDto;
import com.aspire.asat.cms.dto.client.responseDto.PhishingCourseStatisticsCountsDto;
import com.aspire.asat.cms.dto.enums.PhishingCourseDashboardStatus;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.custom.PhishingCourseRepositoryCustom;
import com.aspire.asat.cms.service.impl.PhishingCourseStatisticsServiceImpl;
import com.aspire.asat.cms.util.PhishingCourseStatusResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhishingCourseStatisticsServiceImplTest {

    private static final String CLIENT_ADMIN_ID = "client-admin-1";

    @Mock
    private PhishingCourseRepositoryCustom phishingCourseRepository;

    @InjectMocks
    private PhishingCourseStatisticsServiceImpl service;

    @Test
    void getStatistics_passesThroughRepositoryCounts() {
        PhishingCourseStatisticsCountsDto repoCounts = PhishingCourseStatisticsCountsDto.builder()
                .totalUsers(20)
                .completedUsers(10)
                .inProgressUsers(5)
                .pendingUsers(3)
                .expiredUsers(2)
                .build();
        when(phishingCourseRepository.getStatisticsCounts(CLIENT_ADMIN_ID, null)).thenReturn(repoCounts);

        PhishingCourseStatisticsCountsDto result = service.getStatistics(CLIENT_ADMIN_ID);

        assertEquals(20, result.getTotalUsers());
        assertEquals(10, result.getCompletedUsers());
        assertEquals(5, result.getInProgressUsers());
        assertEquals(3, result.getPendingUsers());
        assertEquals(2, result.getExpiredUsers());
        verify(phishingCourseRepository).getStatisticsCounts(CLIENT_ADMIN_ID, null);
    }

    @Test
    void getStatistics_forwardsSubPackageIdsToRepository() {
        PhishingCourseStatisticsCountsDto repoCounts = PhishingCourseStatisticsCountsDto.builder()
                .totalUsers(4)
                .completedUsers(1)
                .inProgressUsers(1)
                .pendingUsers(1)
                .expiredUsers(1)
                .build();
        List<String> subPackageIds = List.of("mod-sms-1", "mod-sms-2");
        when(phishingCourseRepository.getStatisticsCounts(CLIENT_ADMIN_ID, subPackageIds)).thenReturn(repoCounts);

        PhishingCourseStatisticsCountsDto result = service.getStatistics(CLIENT_ADMIN_ID, subPackageIds);

        assertEquals(4, result.getTotalUsers());
        verify(phishingCourseRepository).getStatisticsCounts(eq(CLIENT_ADMIN_ID), eq(subPackageIds));
    }

    @Test
    void getDetails_wrapsRepositoryResultAndForwardsParameters() {
        List<PhishingCourseEnrollmentDto> items = List.of(
                PhishingCourseEnrollmentDto.builder()
                        .userId("u1")
                        .subPackageId("sp1")
                        .subPackageName("Phishing 101")
                        .assignedDate(LocalDate.of(2024, 6, 1))
                        .expiryDate(LocalDate.of(2024, 7, 1))
                        .status(PhishingCourseDashboardStatus.InProgress.name())
                        .progress(45.0)
                        .build()
        );
        List<String> userIds = List.of("u1", "u2");
        when(phishingCourseRepository.getDetails(CLIENT_ADMIN_ID, 1, 5, userIds)).thenReturn(items);
        when(phishingCourseRepository.countDetails(CLIENT_ADMIN_ID, userIds)).thenReturn(17L);

        AllResponseDto<List<PhishingCourseEnrollmentDto>> result = service.getDetails(CLIENT_ADMIN_ID, 1, 5, userIds);

        assertEquals(1, result.getOffset());
        assertEquals(5, result.getPageSize());
        assertEquals(17L, result.getTotal());
        assertEquals(items, result.getItems());
        verify(phishingCourseRepository).getDetails(eq(CLIENT_ADMIN_ID), eq(1), eq(5), eq(userIds));
        verify(phishingCourseRepository).countDetails(eq(CLIENT_ADMIN_ID), eq(userIds));
    }

    @Test
    void getDetails_passesNullUserIdsThroughWhenNotProvided() {
        when(phishingCourseRepository.getDetails(CLIENT_ADMIN_ID, 0, 10, null)).thenReturn(List.of());
        when(phishingCourseRepository.countDetails(CLIENT_ADMIN_ID, null)).thenReturn(0L);

        AllResponseDto<List<PhishingCourseEnrollmentDto>> result = service.getDetails(CLIENT_ADMIN_ID, 0, 10, null);

        assertEquals(0L, result.getTotal());
        assertEquals(Set.of(), Set.copyOf(result.getItems()));
        verify(phishingCourseRepository).getDetails(eq(CLIENT_ADMIN_ID), eq(0), eq(10), eq(null));
        verify(phishingCourseRepository).countDetails(eq(CLIENT_ADMIN_ID), eq(null));
    }

    @Test
    void resolver_mapsCompletedAndPhishingTrainingCompletedToComplete() {
        UserSubPackage completed = UserSubPackage.builder().status("COMPLETED").build();
        UserSubPackage phishingTraining = UserSubPackage.builder().status("PHISHING_TRAINING_COMPLETED").build();

        assertEquals(PhishingCourseDashboardStatus.complete, PhishingCourseStatusResolver.resolve(completed));
        assertEquals(PhishingCourseDashboardStatus.complete, PhishingCourseStatusResolver.resolve(phishingTraining));
    }

    @Test
    void resolver_treatsCompletedAsCompleteEvenWhenExpiryHasPassed() {
        UserSubPackage completedExpired = UserSubPackage.builder()
                .status("COMPLETED")
                .expiryDate(LocalDate.now().minusDays(10))
                .build();

        assertEquals(PhishingCourseDashboardStatus.complete, PhishingCourseStatusResolver.resolve(completedExpired));
    }

    @Test
    void resolver_returnsExpiredWhenInProgressAndExpiryPassed() {
        UserSubPackage inProgressExpired = UserSubPackage.builder()
                .status("IN_PROGRESS")
                .expiryDate(LocalDate.now().minusDays(1))
                .build();
        UserSubPackage notStartedExpired = UserSubPackage.builder()
                .status("NOT_STARTED")
                .expiryDate(LocalDate.now().minusDays(1))
                .build();

        assertEquals(PhishingCourseDashboardStatus.expired, PhishingCourseStatusResolver.resolve(inProgressExpired));
        assertEquals(PhishingCourseDashboardStatus.expired, PhishingCourseStatusResolver.resolve(notStartedExpired));
    }

    @Test
    void resolver_returnsInProgressForInProgressAndExam() {
        UserSubPackage inProgress = UserSubPackage.builder()
                .status("IN_PROGRESS")
                .expiryDate(LocalDate.now().plusDays(5))
                .build();
        UserSubPackage exam = UserSubPackage.builder()
                .status("EXAM")
                .build();

        assertEquals(PhishingCourseDashboardStatus.InProgress, PhishingCourseStatusResolver.resolve(inProgress));
        assertEquals(PhishingCourseDashboardStatus.InProgress, PhishingCourseStatusResolver.resolve(exam));
    }

    @Test
    void resolver_returnsPendingForNotStartedWithFutureOrNullExpiry() {
        UserSubPackage notStartedFuture = UserSubPackage.builder()
                .status("NOT_STARTED")
                .expiryDate(LocalDate.now().plusDays(7))
                .build();
        UserSubPackage notStartedNullExpiry = UserSubPackage.builder()
                .status("NOT_STARTED")
                .build();
        UserSubPackage unknownStatus = UserSubPackage.builder()
                .status("SOMETHING_ELSE")
                .build();

        assertEquals(PhishingCourseDashboardStatus.pending, PhishingCourseStatusResolver.resolve(notStartedFuture));
        assertEquals(PhishingCourseDashboardStatus.pending, PhishingCourseStatusResolver.resolve(notStartedNullExpiry));
        assertEquals(PhishingCourseDashboardStatus.pending, PhishingCourseStatusResolver.resolve(unknownStatus));
    }
}
