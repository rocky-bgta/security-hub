package com.aspire.asat.cms.service.reports.impl;

import com.aspire.asat.cms.client.CmsNotificationClient;
import com.aspire.asat.cms.client.service.ClientAdminServiceClient;
import com.aspire.asat.cms.dto.certificate.ExpiredCertificateReportRowDTO;
import com.aspire.asat.cms.dto.certificate.ExpiredCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.certificate.IssuedCertificateReportSummaryDTO;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import com.aspire.asat.cms.exception.ResourceNotFoundException;
import com.aspire.asat.cms.model.UserCertificate;
import com.aspire.asat.cms.repository.UserCertificateRepository;
import com.aspire.asat.cms.repository.custom.UserCertificateRepositoryCustom;
import com.aspire.asat.cms.util.CertificateStatusUtil;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import software.amazon.awssdk.services.s3.S3Client;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateReportServiceImplTest {

    @Mock
    private UserCertificateRepository userCertificateRepository;
    @Mock
    private UserCertificateRepositoryCustom userCertificateRepositoryCustom;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private ClientAdminServiceClient clientAdminServiceClient;
    @Mock
    private CmsNotificationClient cmsNotificationClient;
    @Mock
    private S3Client s3Client;

    @InjectMocks
    private CertificateReportServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        setField(service, "s3BucketName", "test-bucket");
        setField(service, "reportThresholdDays", 60);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void getExpiredCertificateReportSummary_OmitsDefaultDateWindow() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userId("admin-1")
                .userType(UserType.CLIENT_ADMIN.name())
                .clientAdminId("admin-1")
                .build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(userCertificateRepositoryCustom.getCertificateReportSummaryStats(
                eq("admin-1"), isNull(), isNull(), eq(60), isNull()))
                .thenReturn(ExpiredCertificateReportSummaryDTO.builder()
                        .totalCertificates(5L)
                        .totalExpiredCertificates(2L)
                        .totalExpiringCertificates(1L)
                        .totalValidCertificates(2L)
                        .build());

        ExpiredCertificateReportSummaryDTO summary = service.getExpiredCertificateReportSummary(
                null, null, null, null, null);

        assertEquals(5L, summary.getTotalCertificates());
        verify(userCertificateRepositoryCustom).getCertificateReportSummaryStats(
                eq("admin-1"), isNull(), isNull(), eq(60), isNull());
    }

    @Test
    void getExpiredCertificateReportRows_MapsExpiredStatusLabel() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userId("admin-1")
                .userType(UserType.CLIENT_ADMIN.name())
                .clientAdminId("admin-1")
                .build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        UserCertificate cert = UserCertificate.builder()
                .certificateId("CRT-1")
                .fullName("Omar Khan")
                .productName("Security Basics")
                .expiryDate(Instant.now().minus(10, ChronoUnit.DAYS))
                .build();
        when(userCertificateRepositoryCustom.findCertificatesForReport(
                eq("admin-1"), isNull(), isNull(), isNull(), isNull(), isNull(), eq(60), eq("expiryDate"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cert)));

        var page = service.getExpiredCertificateReportRows(null, null, null, null, null, null, null, 0, 10);

        ExpiredCertificateReportRowDTO row = page.getContent().get(0);
        assertEquals("Expired", row.getCertificateStatus());
        assertTrue(row.getDays().contains("overdue"));
    }

    @Test
    void resolveMspClientAdminIds_CallsRegistrationClientWhenContextEmpty() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userId("msp-1")
                .userType(UserType.MSP.name())
                .build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(clientAdminServiceClient.getClientAdminIdsByMspId("msp-1")).thenReturn(List.of("ca-1", "ca-2"));
        when(userCertificateRepositoryCustom.getCertificateReportSummaryStatsForClientAdminIds(
                eq(List.of("ca-1", "ca-2")), isNull(), isNull(), eq(60), isNull()))
                .thenReturn(emptySummary());

        service.getExpiredCertificateReportSummary(null, null, null, null, null);

        verify(clientAdminServiceClient).getClientAdminIdsByMspId("msp-1");
    }

    @Test
    void exportCertificatesToExcelAndEmail_LooksUpByBusinessCertificateId() {
        UserCertificate cert = UserCertificate.builder()
                .id("mongo-1")
                .certificateId("CRT-20260101-12345")
                .clientAdminId("admin-1")
                .fullName("User")
                .productName("Course")
                .expiryDate(Instant.now().plus(5, ChronoUnit.DAYS))
                .build();
        when(userCertificateRepository.findByCertificateIdIn(List.of("CRT-20260101-12345")))
                .thenReturn(List.of(cert));
        when(userCertificateRepository.findAllById(List.of("CRT-20260101-12345")))
                .thenReturn(List.of());
        when(clientAdminServiceClient.fetchAndCacheAllClientAdmins(anyBoolean()))
                .thenReturn(Map.of("admin-1", Map.of(
                        "email", "admin@example.com",
                        "organizationName", "Org")));
        when(s3Client.putObject(any(software.amazon.awssdk.services.s3.model.PutObjectRequest.class),
                any(software.amazon.awssdk.core.sync.RequestBody.class))).thenReturn(null);
        when(cmsNotificationClient.sendCertificateExpiringNotification(
                any(), any(), any(), any(), anyInt(), any())).thenReturn(true);

        service.exportCertificatesToExcelAndEmail("admin-1", List.of("CRT-20260101-12345"));

        verify(userCertificateRepository).findByCertificateIdIn(List.of("CRT-20260101-12345"));
        verify(cmsNotificationClient).sendCertificateExpiringNotification(
                eq("admin-1"), eq("admin@example.com"), any(), any(), eq(1), any());
    }

    @Test
    void exportCertificatesToExcelAndEmail_PropagatesNotFound() {
        when(userCertificateRepository.findByCertificateIdIn(anyList())).thenReturn(List.of());
        when(userCertificateRepository.findAllById(anyList())).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> service.exportCertificatesToExcelAndEmail("admin-1", List.of("CRT-missing")));
        verify(cmsNotificationClient, never()).sendCertificateExpiringNotification(
                any(), any(), any(), any(), anyInt(), any());
    }

    @Test
    void getIssuedCertificateReportSummary_DelegatesToRepo() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userId("admin-1")
                .userType(UserType.CLIENT_ADMIN.name())
                .clientAdminId("admin-1")
                .build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(userCertificateRepositoryCustom.getIssuedCertificateReportSummaryStats(
                eq("admin-1"), isNull(), isNull()))
                .thenReturn(IssuedCertificateReportSummaryDTO.builder()
                        .totalIssued(100L).thisMonth(8L).thisQuarter(20L).build());

        IssuedCertificateReportSummaryDTO summary =
                service.getIssuedCertificateReportSummary(null, null, null, null);

        assertEquals(100L, summary.getTotalIssued());
        assertEquals(8L, summary.getThisMonth());
        assertEquals(20L, summary.getThisQuarter());
    }

    @Test
    void getExpiredCertificateReportRows_PassesNullDatesWhenOmitted() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userId("admin-1")
                .userType(UserType.CLIENT_ADMIN.name())
                .clientAdminId("admin-1")
                .build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(userCertificateRepositoryCustom.findCertificatesForReport(
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        service.getExpiredCertificateReportRows(null, null, null, null, null, CertificateStatus.VALID, null, 0, 10);

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(userCertificateRepositoryCustom).findCertificatesForReport(
                eq("admin-1"), isNull(), fromCaptor.capture(), toCaptor.capture(),
                isNull(), eq(CertificateStatus.VALID), eq(60), eq("expiryDate"), any(Pageable.class));
        assertNull(fromCaptor.getValue());
        assertNull(toCaptor.getValue());
    }

    @Test
    void getExpiredCertificateReportSummary_ClientAdminFallsBackToUserId_WhenClientAdminIdMissing() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userId("admin-fallback-1")
                .userType(UserType.CLIENT_ADMIN.name())
                .clientAdminId(null)
                .build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(userCertificateRepositoryCustom.getCertificateReportSummaryStats(
                eq("admin-fallback-1"), isNull(), isNull(), eq(60), isNull()))
                .thenReturn(emptySummary());

        service.getExpiredCertificateReportSummary(null, null, null, null, null);

        verify(userCertificateRepositoryCustom).getCertificateReportSummaryStats(
                eq("admin-fallback-1"), isNull(), isNull(), eq(60), isNull());
    }

    @Test
    void getIssuedCertificateReportRows_UsesCreatedAtDateField() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userId("admin-1")
                .userType(UserType.CLIENT_ADMIN.name())
                .clientAdminId("admin-1")
                .build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(userCertificateRepositoryCustom.findCertificatesForReport(
                any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        service.getIssuedCertificateReportRows(null, null, null, null, null, null, 0, 10);

        verify(userCertificateRepositoryCustom).findCertificatesForReport(
                eq("admin-1"), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(CertificateStatusUtil.EXPIRING_SOON_DAYS), eq("createdAt"), any(Pageable.class));
    }

    private static ExpiredCertificateReportSummaryDTO emptySummary() {
        return ExpiredCertificateReportSummaryDTO.builder()
                .totalCertificates(0L)
                .totalExpiredCertificates(0L)
                .totalExpiringCertificates(0L)
                .totalValidCertificates(0L)
                .build();
    }
}
