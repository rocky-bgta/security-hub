package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.RiskLevel;
import com.aspire.asat.phishing.dto.response.BreachSummaryDto;
import com.aspire.asat.phishing.dto.response.CampaignPerformanceDto;
import com.aspire.asat.phishing.dto.response.UserRiskSummaryDto;
import com.aspire.asat.phishing.service.DashboardService;
import com.aspire.asat.phishing.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportControllerImplTest {

    @Mock
    private ReportService reportService;
    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private ReportControllerImpl reportController;

    @Test
    void getCampaignReports_WithChannel_PassesChannelToService() {
        CampaignPerformanceDto dto = CampaignPerformanceDto.builder()
                .campaignId("camp-1")
                .campaignName("Smish 1")
                .build();
        when(reportService.getCampaignReports(0, 10, "createdAt", "desc", CampaignChannel.SMS))
                .thenReturn(List.of(dto));
        when(reportService.countCampaignReports(CampaignChannel.SMS)).thenReturn(1L);

        ResponseEntity<AllResponseDto<List<CampaignPerformanceDto>>> response =
                reportController.getCampaignReports(0, 10, "createdAt", "desc", CampaignChannel.SMS);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getItems().size());
        assertEquals(1L, response.getBody().getTotal());
        verify(reportService).getCampaignReports(0, 10, "createdAt", "desc", CampaignChannel.SMS);
        verify(reportService).countCampaignReports(CampaignChannel.SMS);
    }

    @Test
    void getUserRiskReport_WithChannel_PassesChannelToService() {
        UserRiskSummaryDto dto = UserRiskSummaryDto.builder()
                .userId("u1")
                .email("user@test.com")
                .riskLevel(RiskLevel.HIGH)
                .build();
        when(reportService.getUserRiskReport(0, 20, "Engineering", "test", RiskLevel.HIGH, "riskScore", "desc", CampaignChannel.VOICE))
                .thenReturn(List.of(dto));
        when(reportService.countUsersForRiskReport("Engineering", "test", RiskLevel.HIGH, CampaignChannel.VOICE))
                .thenReturn(1L);

        ResponseEntity<AllResponseDto<List<UserRiskSummaryDto>>> response =
                reportController.getUserRiskReport(0, 20, "Engineering", "test", RiskLevel.HIGH, "riskScore", "desc", CampaignChannel.VOICE);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getItems().size());
        verify(reportService).getUserRiskReport(0, 20, "Engineering", "test", RiskLevel.HIGH, "riskScore", "desc", CampaignChannel.VOICE);
    }

    @Test
    void exportUserRiskReport_WithChannel_PassesChannelToService() {
        byte[] csv = "Email,Risk\ntest@test.com,50".getBytes();
        when(reportService.exportUserRiskReport("csv", CampaignChannel.SMS)).thenReturn(csv);

        ResponseEntity<byte[]> response = reportController.exportUserRiskReport("csv", CampaignChannel.SMS);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(csv, response.getBody());
        verify(reportService).exportUserRiskReport("csv", CampaignChannel.SMS);
    }

    @Test
    void getBreachSummaryReport_WithChannel_PassesChannelToDashboardService() {
        BreachSummaryDto summary = BreachSummaryDto.builder().totalBreaches(0).build();
        when(dashboardService.getBreachSummary(CampaignChannel.EMAIL)).thenReturn(summary);

        ResponseEntity<ApiResponseDto<BreachSummaryDto>> response =
                reportController.getBreachSummaryReport(CampaignChannel.EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dashboardService).getBreachSummary(CampaignChannel.EMAIL);
    }
}
