package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.dto.response.CampaignPerformanceDto;
import com.aspire.asat.phishing.dto.response.UserRiskSummaryDto;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.UserRiskProfile;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.EmailActivityRepository;
import com.aspire.asat.phishing.repository.UserRiskProfileRepository;
import com.aspire.asat.phishing.repository.custom.RecipientWindowActivityMetrics;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplChannelTest {

    private static final String CLIENT_ID = "client-123";

    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private EmailActivityRepository emailActivityRepository;
    @Mock
    private UserRiskProfileRepository userRiskProfileRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        CurrentUserContext context = CurrentUserContext.builder()
                .clientAdminId(CLIENT_ID)
                .userId(CLIENT_ID)
                .userType("CLIENT_ADMIN")
                .build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
    }

    @Test
    void getCampaignReports_DefaultEmail_QueriesWithEmailChannelAndReportableStatuses() {
        Campaign campaign = Campaign.builder()
                .id("camp-1")
                .campaignName("Q1 Phish")
                .channel(CampaignChannel.EMAIL)
                .status(CampaignStatus.RUNNING)
                .build();
        when(campaignRepository.findWithStatuses(
                eq(CLIENT_ID),
                isNull(),
                eq(ReportServiceImpl.REPORTABLE_CAMPAIGN_STATUSES),
                eq(CampaignChannel.EMAIL),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(campaign)));

        List<CampaignPerformanceDto> reports = reportService.getCampaignReports(0, 10, "createdAt", "desc");

        assertNotNull(reports);
        assertEquals(1, reports.size());
        assertEquals("Q1 Phish", reports.get(0).getCampaignName());
        verify(campaignRepository).findWithStatuses(
                eq(CLIENT_ID),
                isNull(),
                eq(ReportServiceImpl.REPORTABLE_CAMPAIGN_STATUSES),
                eq(CampaignChannel.EMAIL),
                any(Pageable.class));
    }

    @Test
    void getCampaignReports_SmsChannel_QueriesWithSmsChannelAndReportableStatuses() {
        Campaign campaign = Campaign.builder()
                .id("camp-sms-1")
                .campaignName("Q1 Smish")
                .channel(CampaignChannel.SMS)
                .status(CampaignStatus.COMPLETED)
                .build();
        when(campaignRepository.findWithStatuses(
                eq(CLIENT_ID),
                isNull(),
                eq(ReportServiceImpl.REPORTABLE_CAMPAIGN_STATUSES),
                eq(CampaignChannel.SMS),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(campaign)));

        List<CampaignPerformanceDto> reports = reportService.getCampaignReports(0, 10, "createdAt", "desc", CampaignChannel.SMS);

        assertNotNull(reports);
        assertEquals(1, reports.size());
        assertEquals("Q1 Smish", reports.get(0).getCampaignName());
        verify(campaignRepository).findWithStatuses(
                eq(CLIENT_ID),
                isNull(),
                eq(ReportServiceImpl.REPORTABLE_CAMPAIGN_STATUSES),
                eq(CampaignChannel.SMS),
                any(Pageable.class));
    }

    @Test
    void countCampaignReports_SmsChannel_CountsBySmsChannelAndReportableStatuses() {
        when(campaignRepository.countByClientIdAndChannelAndStatuses(
                CLIENT_ID, CampaignChannel.SMS, ReportServiceImpl.REPORTABLE_CAMPAIGN_STATUSES)).thenReturn(7L);

        long count = reportService.countCampaignReports(CampaignChannel.SMS);

        assertEquals(7L, count);
        verify(campaignRepository).countByClientIdAndChannelAndStatuses(
                CLIENT_ID, CampaignChannel.SMS, ReportServiceImpl.REPORTABLE_CAMPAIGN_STATUSES);
    }

    @Test
    void getUserRiskReport_NoCampaignsForChannel_ReturnsEmptyList() {
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.VOICE)).thenReturn(List.of());

        List<UserRiskSummaryDto> result = reportService.getUserRiskReport(
                0, 10, null, null, null, "riskScore", "desc", CampaignChannel.VOICE);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getUserRiskReport_SmsChannel_CalculatesWindowMetricsAndFilters() {
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.SMS))
                .thenReturn(List.of("camp-sms-1", "camp-sms-2"));

        RecipientWindowActivityMetrics user1 = new RecipientWindowActivityMetrics(
                "alice@example.com", 2, 0, 1, 0, 0); // Clicked -> CLICKED risk score (60)
        RecipientWindowActivityMetrics user2 = new RecipientWindowActivityMetrics(
                "bob@example.com", 2, 0, 2, 1, 0);   // Submitted -> DATA_SUBMITTED risk score (90)
        RecipientWindowActivityMetrics user3 = new RecipientWindowActivityMetrics(
                "charlie@example.com", 2, 0, 0, 0, 0); // Delivered only -> NONE risk score (0)

        when(emailActivityRepository.aggregateRecipientMetricsForWindow(eq(CLIENT_ID), any(), any(), eq(List.of("camp-sms-1", "camp-sms-2")), any()))
                .thenReturn(List.of(user1, user2, user3));

        UserRiskProfile profileBob = UserRiskProfile.builder()
                .userId("u-bob")
                .email("bob@example.com")
                .firstName("Bob")
                .lastName("Smith")
                .department("Engineering")
                .campaignsTargeted(5)
                .build();
        UserRiskProfile profileAlice = UserRiskProfile.builder()
                .userId("u-alice")
                .email("alice@example.com")
                .firstName("Alice")
                .lastName("Jones")
                .department("Finance")
                .campaignsTargeted(3)
                .build();

        when(userRiskProfileRepository.findByClientIdAndEmailIn(eq(CLIENT_ID), anySet()))
                .thenReturn(List.of(profileBob, profileAlice));

        // Get sorted by riskScore desc
        List<UserRiskSummaryDto> all = reportService.getUserRiskReport(
                0, 10, null, null, null, "riskScore", "desc", CampaignChannel.SMS);

        assertEquals(2, all.size());
        assertEquals("bob@example.com", all.get(0).getEmail());
        assertEquals("Bob Smith", all.get(0).getFullName());
        assertEquals("Engineering", all.get(0).getDepartment());
        assertEquals(100.0, all.get(0).getRiskScore());
        assertEquals(RiskLevel.CRITICAL, all.get(0).getRiskLevel());
        assertEquals(1, all.get(0).getDataSubmissions());
        assertEquals(2, all.get(0).getEmailsClicked());

        assertEquals("alice@example.com", all.get(1).getEmail());
        assertEquals(75.0, all.get(1).getRiskScore());
        assertEquals(RiskLevel.CRITICAL, all.get(1).getRiskLevel());

        // Test filter by department
        List<UserRiskSummaryDto> financeOnly = reportService.getUserRiskReport(
                0, 10, "Finance", null, null, "riskScore", "desc", CampaignChannel.SMS);
        assertEquals(1, financeOnly.size());
        assertEquals("alice@example.com", financeOnly.get(0).getEmail());

        // Test filter by search
        List<UserRiskSummaryDto> searchBob = reportService.getUserRiskReport(
                0, 10, null, "smith", null, "riskScore", "desc", CampaignChannel.SMS);
        assertEquals(1, searchBob.size());
        assertEquals("bob@example.com", searchBob.get(0).getEmail());

        // Test count
        long count = reportService.countUsersForRiskReport(null, null, null, CampaignChannel.SMS);
        assertEquals(2L, count);
    }

    @Test
    void exportUserRiskReport_Csv_GeneratesCsvContent() {
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.SMS))
                .thenReturn(List.of("camp-sms-1"));

        RecipientWindowActivityMetrics user1 = new RecipientWindowActivityMetrics(
                "alice@example.com", 2, 0, 1, 0, 0);
        when(emailActivityRepository.aggregateRecipientMetricsForWindow(eq(CLIENT_ID), any(), any(), anyList(), any()))
                .thenReturn(List.of(user1));

        UserRiskProfile profileAlice = UserRiskProfile.builder()
                .userId("u-alice")
                .email("alice@example.com")
                .firstName("Alice")
                .lastName("Jones")
                .department("Finance")
                .build();
        when(userRiskProfileRepository.findByClientIdAndEmailIn(eq(CLIENT_ID), anySet()))
                .thenReturn(List.of(profileAlice));

        byte[] csvBytes = reportService.exportUserRiskReport("csv", CampaignChannel.SMS);

        assertNotNull(csvBytes);
        String csv = new String(csvBytes);
        assertTrue(csv.contains("Email,Name,Department,Risk Level,Risk Score"));
        assertTrue(csv.contains("alice@example.com"));
        assertTrue(csv.contains("Alice Jones"));
        assertTrue(csv.contains("Finance"));
    }

    @Test
    void getUserRiskReport_SmsChannel_ExcludesRowsWithNullUserId() {
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.SMS))
                .thenReturn(List.of("camp-sms-1"));

        RecipientWindowActivityMetrics user2 = new RecipientWindowActivityMetrics(
                "xtrail-dev-portal-u2@yopmail.com", 0, 0, 2, 1, 0);
        RecipientWindowActivityMetrics user3 = new RecipientWindowActivityMetrics(
                "xtrail-dev-portal-u3@yopmail.com", 0, 0, 1, 0, 0);
        RecipientWindowActivityMetrics user1 = new RecipientWindowActivityMetrics(
                "xtrail-dev-portal-u1@yopmail.com", 0, 0, 1, 0, 0);
        RecipientWindowActivityMetrics phone = new RecipientWindowActivityMetrics(
                "+8801773126589", 6, 0, 0, 0, 0);
        RecipientWindowActivityMetrics phoneVariant = new RecipientWindowActivityMetrics(
                "+88001773126589", 0, 0, 0, 0, 0);

        when(emailActivityRepository.aggregateRecipientMetricsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of("camp-sms-1")), any()))
                .thenReturn(List.of(user2, user3, user1, phone, phoneVariant));

        UserRiskProfile profileTwo = UserRiskProfile.builder()
                .userId("0fd55411-e8fc-442a-9079-d1f9514e9826")
                .email("xtrail-dev-portal-u2@yopmail.com")
                .firstName("Xtrail")
                .lastName("Two")
                .department("OTHERS")
                .build();
        UserRiskProfile profileThree = UserRiskProfile.builder()
                .userId("65dabd5e-b884-427b-bda4-ce0acc026ab6")
                .email("xtrail-dev-portal-u3@yopmail.com")
                .firstName("xtrail dev")
                .lastName("User")
                .department("Audit/Internal Controls")
                .build();
        UserRiskProfile profileOne = UserRiskProfile.builder()
                .userId("f76f6b0d-3016-4bba-97ea-6192bb1b3f09")
                .email("xtrail-dev-portal-u1@yopmail.com")
                .firstName("RTrail")
                .lastName("One")
                .department("IT Support/Helpdesk")
                .build();
        when(userRiskProfileRepository.findByClientIdAndEmailIn(eq(CLIENT_ID), anySet()))
                .thenReturn(List.of(profileTwo, profileThree, profileOne));

        List<UserRiskSummaryDto> all = reportService.getUserRiskReport(
                0, 10, null, null, null, "riskScore", "desc", CampaignChannel.SMS);

        assertEquals(3, all.size());
        assertTrue(all.stream().noneMatch(u -> u.getUserId() == null));
        assertTrue(all.stream().noneMatch(u -> u.getEmail() != null && u.getEmail().startsWith("+")));
        assertEquals("xtrail-dev-portal-u2@yopmail.com", all.get(0).getEmail());
        assertEquals("xtrail-dev-portal-u3@yopmail.com", all.get(1).getEmail());
        assertEquals("xtrail-dev-portal-u1@yopmail.com", all.get(2).getEmail());
        assertEquals(0, all.get(0).getEmailsReceived());

        long count = reportService.countUsersForRiskReport(null, null, null, CampaignChannel.SMS);
        assertEquals(3L, count);
    }

    @Test
    void exportUserRiskReport_SmsCsv_IncludesXtrailUsers() {
        stubXtrailSmsUserRiskData();

        byte[] csvBytes = reportService.exportUserRiskReport("csv", CampaignChannel.SMS);

        assertNotNull(csvBytes);
        String csv = new String(csvBytes, StandardCharsets.UTF_8);
        assertTrue(csv.contains("Email,Name,Department,Risk Level,Risk Score"));
        assertTrue(csv.contains("xtrail-dev-portal-u2@yopmail.com"));
        assertTrue(csv.contains("Xtrail Two"));
        assertTrue(csv.contains("xtrail-dev-portal-u1@yopmail.com"));
        assertTrue(csv.contains("RTrail One"));
        assertTrue(csv.contains("xtrail-dev-portal-u3@yopmail.com"));
        assertTrue(csv.contains("xtrail dev User"));
    }

    @Test
    void exportUserRiskReport_SmsPdf_IncludesXtrailUsers() throws Exception {
        stubXtrailSmsUserRiskData();

        byte[] pdfBytes = reportService.exportUserRiskReport("pdf", CampaignChannel.SMS);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 4);
        assertEquals('%', (char) pdfBytes[0]);
        String text = extractPdfText(pdfBytes);
        assertTrue(text.contains("User Risk Report"), text);
        assertTrue(text.contains("xtrail-dev-portal-u2@yopmail.com"), text);
        assertTrue(text.contains("Xtrail Two"), text);
        assertTrue(text.contains("xtrail-dev-portal-u1@yopmail.com"), text);
        assertTrue(text.contains("RTrail One"), text);
        assertTrue(text.contains("xtrail-dev-portal-u3@yopmail.com"), text);
        assertTrue(text.contains("xtrail dev User"), text);
    }

    @Test
    void exportUserRiskReport_SmsExcel_IncludesXtrailUsers() throws Exception {
        stubXtrailSmsUserRiskData();

        byte[] excelBytes = reportService.exportUserRiskReport("excel", CampaignChannel.SMS);

        assertNotNull(excelBytes);
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals(4, sheet.getPhysicalNumberOfRows());
            Row header = sheet.getRow(0);
            assertEquals("Email", header.getCell(0).getStringCellValue());
            assertEquals("Name", header.getCell(1).getStringCellValue());

            assertEquals("xtrail-dev-portal-u2@yopmail.com", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Xtrail Two", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("xtrail-dev-portal-u3@yopmail.com", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals("xtrail-dev-portal-u1@yopmail.com", sheet.getRow(3).getCell(0).getStringCellValue());
            assertEquals("RTrail One", sheet.getRow(3).getCell(1).getStringCellValue());
        }
    }

    @Test
    void exportUserRiskReport_DefaultEmail_DoesNotIncludeSmsOnlyUsers() throws Exception {
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.EMAIL))
                .thenReturn(List.of());

        byte[] csvBytes = reportService.exportUserRiskReport("csv");
        String csv = new String(csvBytes, StandardCharsets.UTF_8);
        assertTrue(csv.contains("Email,Name,Department,Risk Level,Risk Score"));
        assertFalse(csv.contains("xtrail-dev-portal-u2@yopmail.com"));
        assertFalse(csv.contains("Xtrail Two"));

        byte[] pdfBytes = reportService.exportUserRiskReport("pdf");
        String pdfText = extractPdfText(pdfBytes);
        assertTrue(pdfText.contains("User Risk Report"), pdfText);
        assertFalse(pdfText.contains("xtrail-dev-portal-u2@yopmail.com"), pdfText);
        assertFalse(pdfText.contains("Xtrail Two"), pdfText);
        assertFalse(pdfText.contains("RTrail One"), pdfText);
    }

    private void stubXtrailSmsUserRiskData() {
        when(campaignRepository.findIdsByClientIdAndChannel(CLIENT_ID, CampaignChannel.SMS))
                .thenReturn(List.of("camp-sms-1"));

        RecipientWindowActivityMetrics user2 = new RecipientWindowActivityMetrics(
                "xtrail-dev-portal-u2@yopmail.com", 0, 0, 2, 1, 0);
        RecipientWindowActivityMetrics user3 = new RecipientWindowActivityMetrics(
                "xtrail-dev-portal-u3@yopmail.com", 0, 0, 1, 0, 0);
        RecipientWindowActivityMetrics user1 = new RecipientWindowActivityMetrics(
                "xtrail-dev-portal-u1@yopmail.com", 0, 0, 1, 0, 0);

        when(emailActivityRepository.aggregateRecipientMetricsForWindow(
                eq(CLIENT_ID), any(), any(), eq(List.of("camp-sms-1")), any()))
                .thenReturn(List.of(user2, user3, user1));

        UserRiskProfile profileTwo = UserRiskProfile.builder()
                .userId("0fd55411-e8fc-442a-9079-d1f9514e9826")
                .email("xtrail-dev-portal-u2@yopmail.com")
                .firstName("Xtrail")
                .lastName("Two")
                .department("OTHERS")
                .build();
        UserRiskProfile profileThree = UserRiskProfile.builder()
                .userId("65dabd5e-b884-427b-bda4-ce0acc026ab6")
                .email("xtrail-dev-portal-u3@yopmail.com")
                .firstName("xtrail dev")
                .lastName("User")
                .department("Audit/Internal Controls")
                .build();
        UserRiskProfile profileOne = UserRiskProfile.builder()
                .userId("f76f6b0d-3016-4bba-97ea-6192bb1b3f09")
                .email("xtrail-dev-portal-u1@yopmail.com")
                .firstName("RTrail")
                .lastName("One")
                .department("IT Support/Helpdesk")
                .build();
        when(userRiskProfileRepository.findByClientIdAndEmailIn(eq(CLIENT_ID), anySet()))
                .thenReturn(List.of(profileTwo, profileThree, profileOne));
    }

    private static String extractPdfText(byte[] pdfBytes) throws Exception {
        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(pdfBytes))) {
            return new PDFTextStripper().getText(document);
        }
    }
}
