package com.aspire.asat.cms.service.reports.impl;

import com.aspire.asat.cms.dto.reports.PackageAssignmentReportDTO;
import com.aspire.asat.cms.dto.registration.AspireUserBasicDto;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.custom.SubPackageRepositoryCustom;
import com.aspire.asat.cms.repository.custom.UserPackageRepositoryCustom;
import com.aspire.asat.cms.service.external.RegistrationServiceClient;
import com.aspire.asat.cms.service.reports.ClientReportScopeResolver;
import com.aspire.asat.cms.util.PackageAssignmentStatusUtil;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PackageAssignmentReportServiceImplTest {

    @Mock
    private UserSubPackageRepository userSubPackageRepository;
    @Mock
    private SubPackageRepository subPackageRepository;
    @Mock
    private SubPackageRepositoryCustom subPackageRepositoryCustom;
    @Mock
    private UserPackageRepositoryCustom userPackageRepositoryCustom;
    @Mock
    private RegistrationServiceClient registrationServiceClient;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Spy
    private ClientReportScopeResolver clientReportScopeResolver = new ClientReportScopeResolver();

    @InjectMocks
    private PackageAssignmentReportServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(aspireAdminContext());
    }

    @Test
    void getPackageAssignmentReport_shouldComposeSummaryAndLogForAdmin() {
        stubSummaryCounts(null, null);
        when(userSubPackageRepository.findAssignmentsForReport(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(List.of(sampleAssignment()));
        when(userSubPackageRepository.countAssignments(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(1L);
        when(subPackageRepository.findAllById(any())).thenReturn(List.of(sampleSubPackage()));
        when(userPackageRepositoryCustom.getBundleNamesByPackageIds(any())).thenReturn(Map.of("pkg-1", "Enterprise Security Suite"));
        when(registrationServiceClient.getUsersByIds(List.of("user-1"))).thenReturn(List.of(
                AspireUserBasicDto.builder().userId("user-1").fullName("Ahmed Ali").build()));

        PackageAssignmentReportDTO report = service.getPackageAssignmentReport(
                null, null, null, null, null, null, 0, 20);

        assertEquals(2L, report.getSummary().getTotalPackages());
        assertEquals(10L, report.getSummary().getTotalSubPackages());
        assertEquals(7L, report.getSummary().getActiveAssignments());
        assertEquals(2L, report.getSummary().getExpiringSoon());
        assertEquals(1L, report.getSummary().getExpired());
        assertEquals(3L, report.getSummary().getCompleteAssignments());
        assertEquals(1, report.getAssignmentLog().getItems().size());
        assertEquals("Ahmed Ali", report.getAssignmentLog().getItems().get(0).getUser());
        assertEquals("Enterprise Security Suite", report.getAssignmentLog().getItems().get(0).getPackageName());
        assertEquals("Admin", report.getAssignmentLog().getItems().get(0).getAssignedBy());
        verify(registrationServiceClient).getUsersByIds(List.of("user-1"));
        verify(subPackageRepositoryCustom).countSubPackagesForReport(isNull(), isNull());
    }

    @Test
    void getPackageAssignmentReport_shouldScopeToClientAdminFromContext() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(clientAdminContext("ctx-client"));
        stubSummaryCounts("ctx-client", null);
        when(userSubPackageRepository.findAssignmentsForReport(
                eq("ctx-client"), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(List.of());
        when(userSubPackageRepository.countAssignments(
                eq("ctx-client"), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(0L);

        service.getPackageAssignmentReport("other-client", null, null, null, null, null, 0, 20);

        verify(userSubPackageRepository).findAssignmentsForReport(
                eq("ctx-client"), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20));
    }

    @Test
    void getPackageAssignmentReport_shouldUseMspClientAdminIdsFromContext() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspContext(List.of("client-1", "client-2")));
        when(userSubPackageRepository.countDistinctParentPackages(isNull(), eq(List.of("client-1", "client-2")), isNull(), isNull()))
                .thenReturn(0L);
        when(subPackageRepositoryCustom.countSubPackagesForReport(isNull(), eq(List.of("client-1", "client-2"))))
                .thenReturn(0L);
        when(userSubPackageRepository.countByExpiryBucket(isNull(), eq(List.of("client-1", "client-2")), isNull(), isNull(), any()))
                .thenReturn(0L);
        when(userSubPackageRepository.findAssignmentsForReport(
                isNull(), eq(List.of("client-1", "client-2")), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(List.of());
        when(userSubPackageRepository.countAssignments(
                isNull(), eq(List.of("client-1", "client-2")), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(0L);

        service.getPackageAssignmentReport(null, null, null, null, null, null, 0, 20);

        verify(userSubPackageRepository).countAssignments(
                isNull(), eq(List.of("client-1", "client-2")), isNull(), isNull(), isNull(), isNull());
        verify(subPackageRepositoryCustom).countSubPackagesForReport(
                isNull(), eq(List.of("client-1", "client-2")));
    }

    @Test
    void getPackageAssignmentReport_shouldReturnEmptyWhenMspHasNoClientAdminIds() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspContext(List.of()));

        PackageAssignmentReportDTO report = service.getPackageAssignmentReport(
                null, null, null, null, null, null, 0, 20);

        assertEquals(0L, report.getSummary().getTotalSubPackages());
        assertEquals(0L, report.getSummary().getCompleteAssignments());
        assertTrue(report.getAssignmentLog().getItems().isEmpty());
    }

    @Test
    void getPackageAssignmentReport_shouldFilterByCompleteStatus() {
        stubSummaryCounts(null, null);
        when(userSubPackageRepository.findAssignmentsForReport(
                isNull(), isNull(), isNull(), eq("COMPLETE"), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(List.of(completedAssignment()));
        when(userSubPackageRepository.countAssignments(
                isNull(), isNull(), isNull(), eq("COMPLETE"), isNull(), isNull()))
                .thenReturn(1L);
        when(subPackageRepository.findAllById(any())).thenReturn(List.of(sampleSubPackage()));
        when(userPackageRepositoryCustom.getBundleNamesByPackageIds(any())).thenReturn(Map.of("pkg-1", "Enterprise Security Suite"));

        PackageAssignmentReportDTO report = service.getPackageAssignmentReport(
                null, null, null, "COMPLETE", null, null, 0, 20);

        assertEquals(PackageAssignmentStatusUtil.STATUS_COMPLETE,
                report.getAssignmentLog().getItems().get(0).getStatus());
        verify(userSubPackageRepository).findAssignmentsForReport(
                isNull(), isNull(), isNull(), eq("COMPLETE"), isNull(), isNull(), eq(0), eq(20));
    }

    @Test
    void getPackageAssignmentReport_shouldDisplayCompleteForCompletedRow() {
        stubSummaryCounts(null, null);
        when(userSubPackageRepository.findAssignmentsForReport(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(List.of(completedAssignment()));
        when(userSubPackageRepository.countAssignments(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(1L);
        when(subPackageRepository.findAllById(any())).thenReturn(List.of(sampleSubPackage()));
        when(userPackageRepositoryCustom.getBundleNamesByPackageIds(any())).thenReturn(Map.of("pkg-1", "Enterprise Security Suite"));

        PackageAssignmentReportDTO report = service.getPackageAssignmentReport(
                null, null, null, null, null, null, 0, 20);

        assertEquals(PackageAssignmentStatusUtil.STATUS_COMPLETE,
                report.getAssignmentLog().getItems().get(0).getStatus());
    }

    @Test
    void exportPackageAssignmentReportCsv_shouldIncludeAssignedByAdminAndResolveAspireUserNames() {
        when(userSubPackageRepository.findAssignmentsForReportExport(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(1000)))
                .thenReturn(List.of(sampleAssignment()));
        when(subPackageRepository.findAllById(any())).thenReturn(List.of(sampleSubPackage()));
        when(userPackageRepositoryCustom.getBundleNamesByPackageIds(any())).thenReturn(Map.of("pkg-1", "Enterprise Security Suite"));
        when(registrationServiceClient.getUsersByIds(List.of("user-1"))).thenReturn(List.of(
                AspireUserBasicDto.builder().userId("user-1").fullName("Ahmed Ali").build()));

        byte[] csv = service.exportPackageAssignmentReportCsv(null, null, null, null, null, null);
        String content = new String(csv, StandardCharsets.UTF_8);

        assertTrue(content.startsWith("\uFEFF"));
        assertTrue(content.contains("User,Package,Sub Package,Assigned,Expiry,Status,Assigned By"));
        assertTrue(content.contains("Admin"));
        assertTrue(content.contains("Ahmed Ali"));
        assertTrue(content.contains("Enterprise Security Suite"));
        verify(registrationServiceClient).getUsersByIds(List.of("user-1"));
    }

    @Test
    void exportPackageAssignmentReportCsv_shouldRespectCompleteFilter() {
        when(userSubPackageRepository.findAssignmentsForReportExport(
                isNull(), isNull(), isNull(), eq("COMPLETE"), isNull(), isNull(), eq(0), eq(1000)))
                .thenReturn(List.of(completedAssignment()));
        when(subPackageRepository.findAllById(any())).thenReturn(List.of(sampleSubPackage()));
        when(userPackageRepositoryCustom.getBundleNamesByPackageIds(any())).thenReturn(Map.of("pkg-1", "Enterprise Security Suite"));
        when(registrationServiceClient.getUsersByIds(List.of("user-2"))).thenReturn(List.of(
                AspireUserBasicDto.builder().userId("user-2").fullName("Completed User").build()));

        byte[] csv = service.exportPackageAssignmentReportCsv(null, null, null, "COMPLETE", null, null);
        String content = new String(csv, StandardCharsets.UTF_8);

        assertTrue(content.contains("Complete"));
        assertTrue(content.contains("Completed User"));
        verify(userSubPackageRepository).findAssignmentsForReportExport(
                isNull(), isNull(), isNull(), eq("COMPLETE"), isNull(), isNull(), eq(0), eq(1000));
        verify(registrationServiceClient).getUsersByIds(List.of("user-2"));
    }

    @Test
    void exportPackageAssignmentReportCsv_shouldReturnHeaderOnlyForEmptyMspScope() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspContext(List.of()));

        byte[] csv = service.exportPackageAssignmentReportCsv(null, null, null, null, null, null);
        String content = new String(csv, StandardCharsets.UTF_8);

        assertTrue(content.startsWith("\uFEFF"));
        assertTrue(content.contains("User,Package,Sub Package,Assigned,Expiry,Status,Assigned By"));
        assertEquals(1, content.trim().split("\n").length);
    }

    @Test
    void statusUtil_shouldClassifyExpiringAt80PercentProgress() {
        LocalDate assigned = LocalDate.of(2026, 1, 1);
        LocalDate expiry = LocalDate.of(2027, 1, 1);
        LocalDate today = LocalDate.of(2026, 10, 20);
        String status = PackageAssignmentStatusUtil.resolveDisplayStatus(
                assigned, expiry, "IN_PROGRESS", today);
        assertEquals(PackageAssignmentStatusUtil.STATUS_EXPIRING, status);
    }

    @Test
    void statusUtil_shouldClassifyActiveBefore80PercentProgress() {
        LocalDate assigned = LocalDate.of(2026, 1, 1);
        LocalDate expiry = LocalDate.of(2027, 1, 1);
        LocalDate today = LocalDate.of(2026, 6, 1);
        String status = PackageAssignmentStatusUtil.resolveDisplayStatus(
                assigned, expiry, "IN_PROGRESS", today);
        assertEquals(PackageAssignmentStatusUtil.STATUS_ACTIVE, status);
    }

    @Test
    void statusUtil_shouldMarkCompletedAsCompleteEvenWhenPastExpiry() {
        LocalDate assigned = LocalDate.of(2026, 1, 1);
        LocalDate expiry = LocalDate.of(2026, 6, 1);
        LocalDate today = LocalDate.of(2026, 6, 20);
        String status = PackageAssignmentStatusUtil.resolveDisplayStatus(
                assigned, expiry, "COMPLETED", today);
        assertEquals(PackageAssignmentStatusUtil.STATUS_COMPLETE, status);
    }

    private void stubSummaryCounts(String clientAdminId, List<String> clientAdminIds) {
        when(subPackageRepositoryCustom.countSubPackagesForReport(eq(clientAdminId), eq(clientAdminIds)))
                .thenReturn(10L);
        when(userSubPackageRepository.countDistinctParentPackages(eq(clientAdminId), eq(clientAdminIds), isNull(), isNull()))
                .thenReturn(2L);
        when(userSubPackageRepository.countByExpiryBucket(eq(clientAdminId), eq(clientAdminIds), isNull(), isNull(), eq("ACTIVE")))
                .thenReturn(7L);
        when(userSubPackageRepository.countByExpiryBucket(eq(clientAdminId), eq(clientAdminIds), isNull(), isNull(), eq("EXPIRING")))
                .thenReturn(2L);
        when(userSubPackageRepository.countByExpiryBucket(eq(clientAdminId), eq(clientAdminIds), isNull(), isNull(), eq("EXPIRED")))
                .thenReturn(1L);
        when(userSubPackageRepository.countByExpiryBucket(eq(clientAdminId), eq(clientAdminIds), isNull(), isNull(), eq("COMPLETE")))
                .thenReturn(3L);
    }

    private static CurrentUserContext aspireAdminContext() {
        return CurrentUserContext.builder()
                .userId("admin-1")
                .userType(UserType.ASPIRE_ADMIN.name())
                .build();
    }

    private static CurrentUserContext clientAdminContext(String clientAdminId) {
        return CurrentUserContext.builder()
                .userId(clientAdminId)
                .clientAdminId(clientAdminId)
                .userType(UserType.CLIENT_ADMIN.name())
                .build();
    }

    private static CurrentUserContext mspContext(List<String> clientAdminIds) {
        return CurrentUserContext.builder()
                .userId("msp-1")
                .userType(UserType.MSP.name())
                .clientAdminIds(clientAdminIds)
                .build();
    }

    private static UserSubPackage sampleAssignment() {
        return UserSubPackage.builder()
                .userId("user-1")
                .userEmail("ahmed@company.com")
                .subPackageId("sub-1")
                .subPackageName("Enterprise Security Suite")
                .assignedDate(LocalDate.of(2026, 1, 15))
                .expiryDate(LocalDate.of(2027, 1, 15))
                .status("IN_PROGRESS")
                .build();
    }

    private static UserSubPackage completedAssignment() {
        return UserSubPackage.builder()
                .userId("user-2")
                .userEmail("completed@company.com")
                .subPackageId("sub-1")
                .subPackageName("Enterprise Security Suite")
                .assignedDate(LocalDate.of(2025, 1, 1))
                .expiryDate(LocalDate.of(2025, 6, 1))
                .status("COMPLETED")
                .build();
    }

    private static SubPackage sampleSubPackage() {
        return SubPackage.builder()
                .id("sub-1")
                .packageId("pkg-1")
                .name("Enterprise Security Suite")
                .build();
    }
}
