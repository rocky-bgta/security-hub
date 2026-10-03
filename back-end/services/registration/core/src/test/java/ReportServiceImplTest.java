import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.data.enums.PackageStatus;
import com.aspire.asat.registration.data.enums.RiskGroup;
import com.aspire.asat.registration.data.enums.UserStatus;
import com.aspire.asat.registration.data.reports.UserSummaryReportDTO;
import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.EndUserPackageRepository;
import com.aspire.asat.registration.repository.custom.ClientProductRepositoryCustom;
import com.aspire.asat.registration.repository.msp.MspUsersRepository;
import com.aspire.asat.registration.service.impl.ReportServiceImpl;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportServiceImplTest {

    @Mock
    private AspireUserRepository aspireUserRepository;

    @Mock
    private ClientAdminRepository clientAdminRepository;

    @Mock
    private ClientProductRepositoryCustom clientProductRepositoryCustom;

    @Mock
    private EndUserPackageRepository endUserPackageRepository;

    @Mock
    private MspUsersRepository mspUsersRepository;

    @Mock
    private UserCurrentContextService userCurrentContextService;

    @InjectMocks
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(aspireAdminContext());
        when(aspireUserRepository.countByStatus(UserStatus.SUSPEND.name())).thenReturn(0L);
        when(clientAdminRepository.count()).thenReturn(0L);
        when(clientProductRepositoryCustom.sumActiveValidLicenseCount(any())).thenReturn(0L);
        when(endUserPackageRepository.countByStatus(PackageStatus.ASSIGNED.name())).thenReturn(0L);
        when(mspUsersRepository.count()).thenReturn(0L);
    }

    @Test
    void getUserSummaryReport_shouldComposeTotalsTrendAndDetails() {
        when(aspireUserRepository.count()).thenReturn(100L);
        when(aspireUserRepository.countByStatus(UserStatus.ACTIVE.name())).thenReturn(80L);
        when(aspireUserRepository.countByStatus(UserStatus.SUSPEND.name())).thenReturn(5L);
        when(aspireUserRepository.countUsersCreatedSince(any(), isNull(), isNull())).thenReturn(12L);
        when(aspireUserRepository.getUserGrowthTrend(eq(6), isNull(), isNull())).thenReturn(List.of());
        when(aspireUserRepository.findUsersForReport(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(List.of(sampleUser()));
        when(aspireUserRepository.countUsersForReport(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull()))
                .thenReturn(1L);
        when(clientAdminRepository.count()).thenReturn(11L);
        when(clientProductRepositoryCustom.sumActiveValidLicenseCount(any())).thenReturn(45L);
        when(endUserPackageRepository.countByStatus(PackageStatus.ASSIGNED.name())).thenReturn(19L);
        when(mspUsersRepository.count()).thenReturn(7L);

        UserSummaryReportDTO report = reportService.getUserSummaryReport(
                null, null, null, null, null, null,
                null, null, 0, 20, 6);

        assertNotNull(report);
        assertEquals(100, report.getTotals().getTotalUsers());
        assertEquals(80, report.getTotals().getActiveUsers());
        assertEquals(5, report.getTotals().getSuspendedUsers());
        assertEquals(12, report.getTotals().getNewSignupsLast30Days());
        assertEquals(1, report.getDetails().getItems().size());
        assertEquals("Ahmed Hassan", report.getDetails().getItems().get(0).getName());
        assertEquals(RiskGroup.LOW_RISK, report.getDetails().getItems().get(0).getRiskGroup());
        assertNotNull(report.getUserCardInfo());
        assertEquals(7L, report.getUserCardInfo().getTotalMsp());
        assertEquals(11L, report.getUserCardInfo().getTotalClientAdmin());
        assertEquals(45L, report.getUserCardInfo().getTotalLicenseUser());
        assertEquals(19L, report.getUserCardInfo().getTotalActiveUser());
        assertEquals(5L, report.getUserCardInfo().getTotalSuspendedUser());
    }

    @Test
    void getUserSummaryReport_shouldPassCreatedAtRangeToRepository() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant toExclusive = Instant.parse("2026-02-01T00:00:00Z");

        when(aspireUserRepository.count()).thenReturn(0L);
        when(aspireUserRepository.countByStatus(any())).thenReturn(0L);
        when(aspireUserRepository.countUsersCreatedSince(any(), isNull(), isNull())).thenReturn(0L);
        when(aspireUserRepository.getUserGrowthTrend(anyInt(), isNull(), isNull())).thenReturn(List.of());
        when(aspireUserRepository.findUsersForReport(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(from), eq(toExclusive), eq(0), eq(20)))
                .thenReturn(List.of());
        when(aspireUserRepository.countUsersForReport(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(from), eq(toExclusive)))
                .thenReturn(0L);

        reportService.getUserSummaryReport(
                null, null, null, null, null, null,
                from, toExclusive, 0, 20, 6);

        verify(aspireUserRepository).findUsersForReport(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(from), eq(toExclusive), eq(0), eq(20));
        verify(aspireUserRepository).countUsersForReport(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(from), eq(toExclusive));
    }

    @Test
    void getUserSummaryReport_shouldScopeToClientAdminFromContext() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(clientAdminContext("ctx-client-id"));
        when(aspireUserRepository.countAllScoped(eq("ctx-client-id"), isNull())).thenReturn(10L);
        when(aspireUserRepository.countByStatusScoped(eq(UserStatus.ACTIVE.name()), eq("ctx-client-id"), isNull())).thenReturn(8L);
        when(aspireUserRepository.countByStatusScoped(eq(UserStatus.SUSPEND.name()), eq("ctx-client-id"), isNull())).thenReturn(1L);
        when(aspireUserRepository.countUsersCreatedSince(any(), eq("ctx-client-id"), isNull())).thenReturn(2L);
        when(aspireUserRepository.getUserGrowthTrend(eq(6), eq("ctx-client-id"), isNull())).thenReturn(List.of());
        when(aspireUserRepository.findUsersForReport(
                isNull(), isNull(), isNull(), isNull(), eq("ctx-client-id"), isNull(),
                isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(List.of());
        when(aspireUserRepository.countUsersForReport(
                isNull(), isNull(), isNull(), isNull(), eq("ctx-client-id"), isNull(),
                isNull(), isNull()))
                .thenReturn(0L);

        reportService.getUserSummaryReport(
                "other-client-id", null, null, null, null, null,
                null, null, 0, 20, 6);

        verify(aspireUserRepository).findUsersForReport(
                isNull(), isNull(), isNull(), isNull(), eq("ctx-client-id"), isNull(),
                isNull(), isNull(), eq(0), eq(20));
    }

    @Test
    void getUserSummaryReport_shouldFilterByMspClientAdminIdsFromContext() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspContext(List.of("client-1", "client-2")));
        when(aspireUserRepository.countAllScoped(eq(List.of("client-1", "client-2")))).thenReturn(20L);
        when(aspireUserRepository.countByStatusScoped(eq(UserStatus.ACTIVE.name()), eq(List.of("client-1", "client-2")))).thenReturn(15L);
        when(aspireUserRepository.countByStatusScoped(eq(UserStatus.SUSPEND.name()), eq(List.of("client-1", "client-2")))).thenReturn(2L);
        when(aspireUserRepository.countUsersCreatedSince(any(), eq(List.of("client-1", "client-2")))).thenReturn(3L);
        when(aspireUserRepository.getUserGrowthTrend(eq(6), eq(List.of("client-1", "client-2")))).thenReturn(List.of());
        when(aspireUserRepository.findUsersForReport(
                isNull(), isNull(), isNull(), eq(List.of("client-1", "client-2")), isNull(),
                isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(List.of());
        when(aspireUserRepository.countUsersForReport(
                isNull(), isNull(), isNull(), eq(List.of("client-1", "client-2")), isNull(),
                isNull(), isNull()))
                .thenReturn(0L);

        reportService.getUserSummaryReport(
                null, null, null, null, null, null,
                null, null, 0, 20, 6);

        verify(aspireUserRepository).countAllScoped(eq(List.of("client-1", "client-2")));
    }

    @Test
    void getUserSummaryReport_shouldReturnEmptyReportWhenMspHasNoClientAdminIds() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspContext(List.of()));

        UserSummaryReportDTO report = reportService.getUserSummaryReport(
                null, null, null, null, null, null,
                null, null, 0, 20, 6);

        assertEquals(0L, report.getTotals().getTotalUsers());
        assertTrue(report.getGrowthTrend().isEmpty());
        assertTrue(report.getDetails().getItems().isEmpty());
        assertNotNull(report.getUserCardInfo());
        assertEquals(0L, report.getUserCardInfo().getTotalMsp());
        assertEquals(0L, report.getUserCardInfo().getTotalClientAdmin());
        assertEquals(0L, report.getUserCardInfo().getTotalLicenseUser());
        assertEquals(0L, report.getUserCardInfo().getTotalActiveUser());
        assertEquals(0L, report.getUserCardInfo().getTotalSuspendedUser());
    }

    @Test
    void exportUserDetailsCsv_shouldIncludeHeaderAndUserRow() {
        when(aspireUserRepository.findUsersForReport(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(0), eq(1000)))
                .thenReturn(List.of(sampleUser()));

        byte[] csv = reportService.exportUserDetailsCsv(
                null, null, null, null, null, null, null, null);

        String content = new String(csv, StandardCharsets.UTF_8);
        assertTrue(content.startsWith("\uFEFF"));
        assertTrue(content.contains("Name,Email,Department,Risk Group,Role,Status,Last Login,Created At"));
        assertTrue(content.contains("Ahmed Hassan"));
        assertTrue(content.contains("ahmed@company.com"));
        assertTrue(content.contains("LOW_RISK"));
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

    private static AspireUser sampleUser() {
        return AspireUser.builder()
                .firstName("Ahmed")
                .lastName("Hassan")
                .email("ahmed@company.com")
                .department("HR")
                .riskGroup(RiskGroup.LOW_RISK)
                .userType("USER")
                .status("ACTIVE")
                .lastLoginAt(Instant.parse("2026-03-28T00:00:00Z"))
                .createdAt(Instant.parse("2026-01-15T00:00:00Z"))
                .build();
    }
}
