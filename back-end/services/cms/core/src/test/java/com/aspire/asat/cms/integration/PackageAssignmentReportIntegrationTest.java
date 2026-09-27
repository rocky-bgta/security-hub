package com.aspire.asat.cms.integration;

import com.aspire.asat.cms.dto.reports.PackageAssignmentReportDTO;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.custom.SubPackageRepositoryCustom;
import com.aspire.asat.cms.repository.custom.UserPackageRepositoryCustom;
import com.aspire.asat.cms.repository.custom.impl.SubPackageRepositoryCustomImpl;
import com.aspire.asat.cms.repository.custom.impl.UserSubPackageRepositoryCustomImpl;
import com.aspire.asat.cms.service.external.RegistrationServiceClient;
import com.aspire.asat.cms.service.reports.ClientReportScopeResolver;
import com.aspire.asat.cms.service.reports.impl.PackageAssignmentReportServiceImpl;
import com.aspire.asat.cms.util.PackageAssignmentStatusUtil;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.mongodb.client.MongoClients;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Testcontainers(disabledWithoutDocker = true)
class PackageAssignmentReportIntegrationTest {

    private static final String CLIENT_ADMIN_ID = "client-admin-it";

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0");

    private MongoTemplate mongoTemplate;
    private UserSubPackageRepositoryCustomImpl repositoryCustom;
    private SubPackageRepositoryCustomImpl subPackageRepositoryCustom;
    private PackageAssignmentReportServiceImpl service;
    private UserSubPackageRepository userSubPackageRepository;
    private SubPackageRepository subPackageRepository;

    @BeforeEach
    void setUp() {
        mongoTemplate = new MongoTemplate(
                new SimpleMongoClientDatabaseFactory(
                        MongoClients.create(mongoDBContainer.getConnectionString()),
                        "package_assignment_report_it"));

        repositoryCustom = new UserSubPackageRepositoryCustomImpl(mongoTemplate);
        subPackageRepositoryCustom = new SubPackageRepositoryCustomImpl(mongoTemplate);
        userSubPackageRepository = mock(UserSubPackageRepository.class);
        subPackageRepository = mock(SubPackageRepository.class);
        UserPackageRepositoryCustom userPackageRepositoryCustom = mock(UserPackageRepositoryCustom.class);
        RegistrationServiceClient registrationServiceClient = mock(RegistrationServiceClient.class);
        UserCurrentContextService userCurrentContextService = mock(UserCurrentContextService.class);

        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder()
                        .userId("admin-it")
                        .userType(UserType.ASPIRE_ADMIN.name())
                        .build());

        when(userSubPackageRepository.countDistinctParentPackages(
                nullable(String.class), nullable(List.class), nullable(LocalDate.class), nullable(LocalDate.class)))
                .thenAnswer(inv -> repositoryCustom.countDistinctParentPackages(
                        inv.getArgument(0), inv.getArgument(1), inv.getArgument(2), inv.getArgument(3)));
        when(userSubPackageRepository.countAssignments(
                nullable(String.class), nullable(List.class), nullable(String.class),
                nullable(String.class), nullable(LocalDate.class), nullable(LocalDate.class)))
                .thenAnswer(inv -> repositoryCustom.countAssignments(
                        inv.getArgument(0), inv.getArgument(1), inv.getArgument(2),
                        inv.getArgument(3), inv.getArgument(4), inv.getArgument(5)));
        when(userSubPackageRepository.countByExpiryBucket(
                nullable(String.class), nullable(List.class), nullable(LocalDate.class),
                nullable(LocalDate.class), nullable(String.class)))
                .thenAnswer(inv -> repositoryCustom.countByExpiryBucket(
                        inv.getArgument(0), inv.getArgument(1), inv.getArgument(2),
                        inv.getArgument(3), inv.getArgument(4)));
        when(userSubPackageRepository.findAssignmentsForReport(
                nullable(String.class), nullable(List.class), nullable(String.class),
                nullable(String.class), nullable(LocalDate.class), nullable(LocalDate.class),
                anyInt(), anyInt()))
                .thenAnswer(inv -> repositoryCustom.findAssignmentsForReport(
                        inv.getArgument(0), inv.getArgument(1), inv.getArgument(2),
                        inv.getArgument(3), inv.getArgument(4), inv.getArgument(5),
                        inv.getArgument(6), inv.getArgument(7)));
        when(userSubPackageRepository.findAssignmentsForReportExport(
                nullable(String.class), nullable(List.class), nullable(String.class),
                nullable(String.class), nullable(LocalDate.class), nullable(LocalDate.class),
                anyInt(), anyInt()))
                .thenAnswer(inv -> repositoryCustom.findAssignmentsForReportExport(
                        inv.getArgument(0), inv.getArgument(1), inv.getArgument(2),
                        inv.getArgument(3), inv.getArgument(4), inv.getArgument(5),
                        inv.getArgument(6), inv.getArgument(7)));

        when(subPackageRepository.findAllById(any())).thenReturn(List.of(
                SubPackage.builder().id("sub-active").packageId("pkg-1").build(),
                SubPackage.builder().id("sub-expiring").packageId("pkg-1").build(),
                SubPackage.builder().id("sub-expired").packageId("pkg-2").build(),
                SubPackage.builder().id("sub-complete").packageId("pkg-2").build()));
        when(userPackageRepositoryCustom.getBundleNamesByPackageIds(any()))
                .thenReturn(Map.of("pkg-1", "Security Bundle", "pkg-2", "Compliance Bundle"));

        service = new PackageAssignmentReportServiceImpl(
                userSubPackageRepository,
                subPackageRepository,
                subPackageRepositoryCustom,
                userPackageRepositoryCustom,
                registrationServiceClient,
                userCurrentContextService,
                new ClientReportScopeResolver());

        seedAssignments();
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.getDb().drop();
    }

    @Test
    void repository_shouldCountExpiryBucketsWith80PercentRules() {
        LocalDate today = LocalDate.now();

        long active = repositoryCustom.countByExpiryBucket(CLIENT_ADMIN_ID, null, null, null, "ACTIVE");
        long expiring = repositoryCustom.countByExpiryBucket(CLIENT_ADMIN_ID, null, null, null, "EXPIRING");
        long expired = repositoryCustom.countByExpiryBucket(CLIENT_ADMIN_ID, null, null, null, "EXPIRED");
        long complete = repositoryCustom.countByExpiryBucket(CLIENT_ADMIN_ID, null, null, null, "COMPLETE");

        assertEquals(1L, active);
        assertEquals(1L, expiring);
        assertEquals(1L, expired);
        assertEquals(1L, complete);

        List<UserSubPackage> completeRows = repositoryCustom.findAssignmentsForReport(
                CLIENT_ADMIN_ID, null, null, "COMPLETE", null, null, 0, 20);
        assertEquals(1, completeRows.size());
        assertEquals("COMPLETED", completeRows.get(0).getStatus());

        List<UserSubPackage> expiredRows = repositoryCustom.findAssignmentsForReport(
                CLIENT_ADMIN_ID, null, null, "EXPIRED", null, null, 0, 20);
        assertEquals(1, expiredRows.size());
        assertTrue(today.isAfter(expiredRows.get(0).getExpiryDate()));
    }

    @Test
    void repository_shouldCountSubPackagesFromSubPackagesCollection() {
        long totalSubPackages = subPackageRepositoryCustom.countSubPackagesForReport(CLIENT_ADMIN_ID, null);
        assertEquals(5L, totalSubPackages);
    }

    @Test
    void service_shouldBuildSummaryAndAssignmentLogFromMongoData() {
        PackageAssignmentReportDTO report = service.getPackageAssignmentReport(
                CLIENT_ADMIN_ID, null, null, null, null, null, 0, 20);

        assertEquals(2L, report.getSummary().getTotalPackages());
        assertEquals(5L, report.getSummary().getTotalSubPackages());
        assertEquals(1L, report.getSummary().getActiveAssignments());
        assertEquals(1L, report.getSummary().getExpiringSoon());
        assertEquals(1L, report.getSummary().getExpired());
        assertEquals(1L, report.getSummary().getCompleteAssignments());
        assertEquals(4, report.getAssignmentLog().getItems().size());
    }

    @Test
    void service_shouldExportCsvWithCompleteFilter() {
        PackageAssignmentReportDTO filtered = service.getPackageAssignmentReport(
                CLIENT_ADMIN_ID, null, null, "COMPLETE", null, null, 0, 20);
        assertEquals(1, filtered.getAssignmentLog().getItems().size());
        assertEquals(PackageAssignmentStatusUtil.STATUS_COMPLETE,
                filtered.getAssignmentLog().getItems().get(0).getStatus());

        byte[] csv = service.exportPackageAssignmentReportCsv(
                CLIENT_ADMIN_ID, null, null, "COMPLETE", null, null);
        String content = new String(csv, StandardCharsets.UTF_8);

        assertTrue(content.startsWith("\uFEFF"));
        assertTrue(content.contains("User,Package,Sub Package,Assigned,Expiry,Status,Assigned By"));
        assertTrue(content.contains("Complete"));
        assertTrue(content.contains("completed@company.com"));
    }

    private void seedAssignments() {
        LocalDate today = LocalDate.now();

        mongoTemplate.insert(UserSubPackage.builder()
                .id("active-1")
                .clientAdminId(CLIENT_ADMIN_ID)
                .userId("active-user")
                .userEmail("active@company.com")
                .subPackageId("sub-active")
                .subPackageName("Active Course")
                .assignedDate(today.minusDays(30))
                .expiryDate(today.plusDays(335))
                .status("IN_PROGRESS")
                .build(), "user_subpackages");

        mongoTemplate.insert(UserSubPackage.builder()
                .id("expiring-1")
                .clientAdminId(CLIENT_ADMIN_ID)
                .userId("expiring-user")
                .userEmail("expiring@company.com")
                .subPackageId("sub-expiring")
                .subPackageName("Expiring Course")
                .assignedDate(today.minusDays(300))
                .expiryDate(today.plusDays(65))
                .status("IN_PROGRESS")
                .build(), "user_subpackages");

        mongoTemplate.insert(UserSubPackage.builder()
                .id("expired-1")
                .clientAdminId(CLIENT_ADMIN_ID)
                .userId("expired-user")
                .userEmail("expired@company.com")
                .subPackageId("sub-expired")
                .subPackageName("Expired Course")
                .assignedDate(today.minusDays(400))
                .expiryDate(today.minusDays(10))
                .status("IN_PROGRESS")
                .build(), "user_subpackages");

        mongoTemplate.insert(UserSubPackage.builder()
                .id("complete-1")
                .clientAdminId(CLIENT_ADMIN_ID)
                .userId("completed-user")
                .userEmail("completed@company.com")
                .subPackageId("sub-complete")
                .subPackageName("Completed Course")
                .assignedDate(today.minusDays(400))
                .expiryDate(today.minusDays(10))
                .status("COMPLETED")
                .build(), "user_subpackages");

        mongoTemplate.insert(SubPackage.builder().id("sub-active").packageId("pkg-1").clientAdminId(CLIENT_ADMIN_ID).build(), "sub_packages");
        mongoTemplate.insert(SubPackage.builder().id("sub-expiring").packageId("pkg-1").clientAdminId(CLIENT_ADMIN_ID).build(), "sub_packages");
        mongoTemplate.insert(SubPackage.builder().id("sub-expired").packageId("pkg-2").clientAdminId(CLIENT_ADMIN_ID).build(), "sub_packages");
        mongoTemplate.insert(SubPackage.builder().id("sub-complete").packageId("pkg-2").clientAdminId(CLIENT_ADMIN_ID).build(), "sub_packages");
        mongoTemplate.insert(SubPackage.builder()
                .id("sub-unassigned")
                .packageId("pkg-2")
                .clientAdminId(CLIENT_ADMIN_ID)
                .name("Unassigned Catalog Sub Package")
                .build(), "sub_packages");
    }
}
