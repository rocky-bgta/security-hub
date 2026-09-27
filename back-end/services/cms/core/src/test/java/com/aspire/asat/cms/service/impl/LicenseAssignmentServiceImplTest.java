package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.license.LicenseAssignmentExportDto;
import com.aspire.asat.cms.dto.license.LicenseAssignmentRowDto;
import com.aspire.asat.cms.dto.registration.AspireUserBasicDto;
import com.aspire.asat.cms.model.Product;
import com.aspire.asat.cms.model.ProductPackage;
import com.aspire.asat.cms.model.SubPackage;
import com.aspire.asat.cms.model.UserSubPackage;
import com.aspire.asat.cms.repository.ProductPackageRepository;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.custom.UserPackageRepositoryCustom;
import com.aspire.asat.cms.service.external.RegistrationServiceClient;
import com.aspire.asat.cms.service.reports.ClientReportScopeResolver;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LicenseAssignmentServiceImplTest {

    @Mock
    private UserSubPackageRepository userSubPackageRepository;
    @Mock
    private SubPackageRepository subPackageRepository;
    @Mock
    private ProductPackageRepository productPackageRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserPackageRepositoryCustom userPackageRepositoryCustom;
    @Mock
    private RegistrationServiceClient registrationServiceClient;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Spy
    private ClientReportScopeResolver clientReportScopeResolver = new ClientReportScopeResolver();

    @InjectMocks
    private LicenseAssignmentServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(clientAdminContext("client-1"));
    }

    @Test
    void listLicenseAssignments_emptyMspScope_returnsEmptyPage() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspContext(List.of()));

        AllResponseDto<List<LicenseAssignmentRowDto>> page = service.listLicenseAssignments(
                null, null, null, null, null, null, null, null, null, 0, 10);

        assertEquals(0L, page.getTotal());
        assertTrue(page.getItems().isEmpty());
        verify(userSubPackageRepository, never()).findLicenseAssignments(
                any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void listLicenseAssignments_enrichesUserProductAndPackage() {
        when(userSubPackageRepository.countLicenseAssignments(
                eq("client-1"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(1L);
        when(userSubPackageRepository.findLicenseAssignments(
                eq("client-1"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(List.of(sampleAssignment()));
        when(subPackageRepository.findAllById(any())).thenReturn(List.of(sampleSubPackage()));
        when(productPackageRepository.findAllById(any())).thenReturn(List.of(sampleProductPackage("Gold")));
        when(productRepository.findAllById(any())).thenReturn(List.of(sampleProduct()));
        when(registrationServiceClient.getUsersByIds(List.of("user-1"))).thenReturn(List.of(
                AspireUserBasicDto.builder()
                        .userId("user-1")
                        .fullName("John Smith")
                        .email("john.smith@crosstewart.com")
                        .department("Human Resources")
                        .build()));

        AllResponseDto<List<LicenseAssignmentRowDto>> page = service.listLicenseAssignments(
                "client-1", null, null, null, null, null, null, null, null, 0, 10);

        assertEquals(1L, page.getTotal());
        LicenseAssignmentRowDto row = page.getItems().get(0);
        assertEquals("John Smith", row.getFullName());
        assertEquals("john.smith@crosstewart.com", row.getEmail());
        assertEquals("Human Resources", row.getDepartment());
        assertEquals("Gold", row.getPackageName());
        assertEquals("Security Awareness Training", row.getProductName());
        assertEquals("Active", row.getStatus());
        verify(registrationServiceClient, never()).findEndUserIds(any(), any(), any());
        verify(userPackageRepositoryCustom, never()).getBundleNamesByPackageIds(any());
    }

    @Test
    void listLicenseAssignments_missingProductPackage_fallsBackToBundleName() {
        when(userSubPackageRepository.countLicenseAssignments(
                any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1L);
        when(userSubPackageRepository.findLicenseAssignments(
                any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(sampleAssignment()));
        when(subPackageRepository.findAllById(any())).thenReturn(List.of(sampleSubPackage()));
        when(productPackageRepository.findAllById(any())).thenReturn(Collections.emptyList());
        when(userPackageRepositoryCustom.getBundleNamesByPackageIds(List.of("pkg-gold")))
                .thenReturn(Map.of("pkg-gold", "Gold"));
        when(productRepository.findAllById(any())).thenReturn(List.of(sampleProduct()));
        when(registrationServiceClient.getUsersByIds(List.of("user-1"))).thenReturn(List.of(
                AspireUserBasicDto.builder().userId("user-1").fullName("John Smith").build()));

        AllResponseDto<List<LicenseAssignmentRowDto>> page = service.listLicenseAssignments(
                "client-1", null, null, null, null, null, null, null, null, 0, 10);

        assertEquals("Gold", page.getItems().get(0).getPackageName());
        verify(userPackageRepositoryCustom).getBundleNamesByPackageIds(List.of("pkg-gold"));
    }

    @Test
    void listLicenseAssignments_missingProductPackageAndBundle_returnsNaPackageName() {
        when(userSubPackageRepository.countLicenseAssignments(
                any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1L);
        when(userSubPackageRepository.findLicenseAssignments(
                any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(sampleAssignment()));
        when(subPackageRepository.findAllById(any())).thenReturn(List.of(sampleSubPackage()));
        when(productPackageRepository.findAllById(any())).thenReturn(Collections.emptyList());
        when(userPackageRepositoryCustom.getBundleNamesByPackageIds(any())).thenReturn(Map.of());
        when(productRepository.findAllById(any())).thenReturn(List.of(sampleProduct()));
        when(registrationServiceClient.getUsersByIds(List.of("user-1"))).thenReturn(List.of(
                AspireUserBasicDto.builder().userId("user-1").fullName("John Smith").build()));

        AllResponseDto<List<LicenseAssignmentRowDto>> page = service.listLicenseAssignments(
                "client-1", null, null, null, null, null, null, null, null, 0, 10);

        assertEquals("pkg-gold", page.getItems().get(0).getPackageId());
        assertEquals("N/A", page.getItems().get(0).getPackageName());
    }

    @Test
    void listLicenseAssignments_searchWithNoMatchingUsers_returnsEmptyWithoutAssignmentQuery() {
        when(registrationServiceClient.findEndUserIds(eq(List.of("client-1")), eq("nobody"), isNull()))
                .thenReturn(List.of());

        AllResponseDto<List<LicenseAssignmentRowDto>> page = service.listLicenseAssignments(
                "client-1", null, "nobody", null, null, null, null, null, null, 0, 10);

        assertEquals(0L, page.getTotal());
        assertTrue(page.getItems().isEmpty());
        verify(userSubPackageRepository, never()).countLicenseAssignments(
                any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void listLicenseAssignments_departmentFilter_passesResolvedUserIds() {
        when(registrationServiceClient.findEndUserIds(eq(List.of("client-1")), isNull(), eq("Finance")))
                .thenReturn(List.of("user-1"));
        when(userSubPackageRepository.countLicenseAssignments(
                eq("client-1"), isNull(), eq(List.of("user-1")), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(0L);
        when(userSubPackageRepository.findLicenseAssignments(
                eq("client-1"), isNull(), eq(List.of("user-1")), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(10)))
                .thenReturn(List.of());

        AllResponseDto<List<LicenseAssignmentRowDto>> page = service.listLicenseAssignments(
                "client-1", null, null, null, null, "Finance", null, null, null, 0, 10);

        assertEquals(0L, page.getTotal());
        verify(registrationServiceClient).findEndUserIds(List.of("client-1"), null, "Finance");
    }

    @Test
    void listLicenseAssignments_departmentWithSlash_passesNameToRegistration() {
        when(registrationServiceClient.findEndUserIds(
                eq(List.of("client-1")), isNull(), eq("Audit/Internal Controls")))
                .thenReturn(List.of("user-1"));
        when(userSubPackageRepository.countLicenseAssignments(
                eq("client-1"), isNull(), eq(List.of("user-1")), isNull(), isNull(),
                eq("EXPIRING_SOON"), isNull(), isNull()))
                .thenReturn(1L);
        when(userSubPackageRepository.findLicenseAssignments(
                eq("client-1"), isNull(), eq(List.of("user-1")), isNull(), isNull(),
                eq("EXPIRING_SOON"), isNull(), isNull(), eq(0), eq(10)))
                .thenReturn(List.of());

        service.listLicenseAssignments(
                "client-1", null, null, null, null,
                "Audit/Internal Controls", "EXPIRING_SOON", null, null, 0, 10);

        verify(registrationServiceClient).findEndUserIds(List.of("client-1"), null, "Audit/Internal Controls");
        verify(userSubPackageRepository).findLicenseAssignments(
                eq("client-1"), isNull(), eq(List.of("user-1")), isNull(), isNull(),
                eq("EXPIRING_SOON"), isNull(), isNull(), eq(0), eq(10));
    }

    @Test
    void listLicenseAssignments_searchProductPackageAndDates_passThroughToRepository() {
        when(registrationServiceClient.findEndUserIds(eq(List.of("client-1")), eq("john"), isNull()))
                .thenReturn(List.of("user-1"));
        when(userSubPackageRepository.countLicenseAssignments(
                eq("client-1"), isNull(), eq(List.of("user-1")), eq("prod-sat"), eq("pkg-gold"),
                eq("ACTIVE"), eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 12, 31))))
                .thenReturn(0L);
        when(userSubPackageRepository.findLicenseAssignments(
                eq("client-1"), isNull(), eq(List.of("user-1")), eq("prod-sat"), eq("pkg-gold"),
                eq("ACTIVE"), eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 12, 31)),
                eq(0), eq(10)))
                .thenReturn(List.of());

        service.listLicenseAssignments(
                "client-1", null, "john", "pkg-gold", "prod-sat",
                null, "ACTIVE", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 0, 10);

        verify(registrationServiceClient).findEndUserIds(List.of("client-1"), "john", null);
        verify(userSubPackageRepository).findLicenseAssignments(
                eq("client-1"), isNull(), eq(List.of("user-1")), eq("prod-sat"), eq("pkg-gold"),
                eq("ACTIVE"), eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 12, 31)),
                eq(0), eq(10));
    }

    @Test
    void listLicenseAssignments_capsPageSizeAt100() {
        when(userSubPackageRepository.countLicenseAssignments(
                any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0L);
        when(userSubPackageRepository.findLicenseAssignments(
                any(), any(), any(), any(), any(), any(), any(), any(), eq(0), eq(100)))
                .thenReturn(List.of());

        service.listLicenseAssignments(
                "client-1", null, null, "pkg-1", "prod-1", null, "ACTIVE",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), 0, 500);

        verify(userSubPackageRepository).findLicenseAssignments(
                eq("client-1"), isNull(), isNull(), eq("prod-1"), eq("pkg-1"), eq("ACTIVE"),
                eq(LocalDate.of(2026, 1, 1)), eq(LocalDate.of(2026, 12, 31)), eq(0), eq(100));
    }

    @Test
    void listLicenseAssignments_missingUserEnrichment_fallsBackToEmail() {
        when(userSubPackageRepository.countLicenseAssignments(
                any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(1L);
        when(userSubPackageRepository.findLicenseAssignments(
                any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(List.of(sampleAssignment()));
        when(subPackageRepository.findAllById(any())).thenReturn(List.of());
        when(productRepository.findAllById(any())).thenReturn(List.of());
        when(registrationServiceClient.getUsersByIds(List.of("user-1"))).thenReturn(List.of());

        AllResponseDto<List<LicenseAssignmentRowDto>> page = service.listLicenseAssignments(
                "client-1", null, null, null, null, null, null, null, null, 0, 10);

        LicenseAssignmentRowDto row = page.getItems().get(0);
        assertEquals("john.smith@crosstewart.com", row.getFullName());
        assertEquals("john.smith@crosstewart.com", row.getEmail());
        assertEquals("N/A", row.getPackageName());
        assertEquals("N/A", row.getProductName());
    }

    @Test
    void exportLicenseAssignments_csv_includesTableColumnsAndRow() {
        stubSingleAssignmentForExport();

        LicenseAssignmentExportDto export = service.exportLicenseAssignments(
                "client-1", null, null, null, null, null, null, null, null, "csv");

        assertEquals("license-assignments.csv", export.getFilename());
        assertEquals("text/csv; charset=UTF-8", export.getContentType());
        String csv = new String(export.getContent(), StandardCharsets.UTF_8);
        assertTrue(csv.contains("User Name,Email,Department,Package,Product,Assigned Date,Expiry Date,Status"));
        assertTrue(csv.contains("John Smith"));
        assertTrue(csv.contains("Human Resources"));
        assertTrue(csv.contains("Gold"));
        assertTrue(csv.contains("Security Awareness Training"));
        assertTrue(csv.contains("2026-04-26"));
        verify(userSubPackageRepository).findLicenseAssignments(
                eq("client-1"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(1000));
    }

    @Test
    void exportLicenseAssignments_xls_returnsExcelWorkbook() throws Exception {
        stubSingleAssignmentForExport();

        LicenseAssignmentExportDto export = service.exportLicenseAssignments(
                "client-1", null, null, null, null, null, null, null, null, "xls");

        assertEquals("license-assignments.xls", export.getFilename());
        assertEquals("application/vnd.ms-excel", export.getContentType());
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(export.getContent()))) {
            assertEquals("John Smith", workbook.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
            assertEquals("Gold", workbook.getSheetAt(0).getRow(1).getCell(3).getStringCellValue());
            assertEquals("Active", workbook.getSheetAt(0).getRow(1).getCell(7).getStringCellValue());
        }
    }

    @Test
    void exportLicenseAssignments_emptyScope_returnsHeaderOnlyCsv() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspContext(List.of()));

        LicenseAssignmentExportDto export = service.exportLicenseAssignments(
                null, null, null, null, null, null, null, null, null, "csv");

        String csv = new String(export.getContent(), StandardCharsets.UTF_8);
        assertTrue(csv.contains("User Name,Email,Department,Package,Product,Assigned Date,Expiry Date,Status"));
        assertEquals(1, csv.trim().split("\n").length);
        verify(userSubPackageRepository, never()).findLicenseAssignments(
                any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void exportLicenseAssignments_unsupportedFormat_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.exportLicenseAssignments(
                "client-1", null, null, null, null, null, null, null, null, "pdf"));
    }

    private void stubSingleAssignmentForExport() {
        when(userSubPackageRepository.findLicenseAssignments(
                eq("client-1"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(1000)))
                .thenReturn(List.of(sampleAssignment()));
        when(subPackageRepository.findAllById(any())).thenReturn(List.of(sampleSubPackage()));
        when(productPackageRepository.findAllById(any())).thenReturn(List.of(sampleProductPackage("Gold")));
        when(productRepository.findAllById(any())).thenReturn(List.of(sampleProduct()));
        when(registrationServiceClient.getUsersByIds(List.of("user-1"))).thenReturn(List.of(
                AspireUserBasicDto.builder()
                        .userId("user-1")
                        .fullName("John Smith")
                        .email("john.smith@crosstewart.com")
                        .department("Human Resources")
                        .build()));
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
                .userEmail("john.smith@crosstewart.com")
                .productId("prod-sat")
                .subPackageId("sub-1")
                .subPackageName("Security Awareness Training")
                .assignedDate(LocalDate.of(2026, 4, 26))
                .expiryDate(LocalDate.of(2027, 4, 26))
                .status("NOT_STARTED")
                .build();
    }

    private static SubPackage sampleSubPackage() {
        return SubPackage.builder()
                .id("sub-1")
                .packageId("pkg-gold")
                .name("Security Awareness Training")
                .build();
    }

    private static ProductPackage sampleProductPackage(String name) {
        return ProductPackage.builder()
                .id("pkg-gold")
                .name(name)
                .build();
    }

    private static Product sampleProduct() {
        return Product.builder()
                .id("prod-sat")
                .productName("Security Awareness Training")
                .build();
    }
}
