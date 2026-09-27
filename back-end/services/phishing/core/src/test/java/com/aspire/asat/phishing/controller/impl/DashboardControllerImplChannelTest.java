package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.response.BreachSummaryDto;
import com.aspire.asat.phishing.dto.response.DashboardOverviewDto;
import com.aspire.asat.phishing.dto.response.EmailStatsDto;
import com.aspire.asat.phishing.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerImplChannelTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardControllerImpl dashboardController;

    @Test
    void getOverview_PassesChannelToService() {
        DashboardOverviewDto dto = DashboardOverviewDto.builder().totalCampaigns(5).build();
        when(dashboardService.getOverview(CampaignChannel.SMS)).thenReturn(dto);

        ResponseEntity<ApiResponseDto<DashboardOverviewDto>> response =
                dashboardController.getOverview(CampaignChannel.SMS);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dashboardService).getOverview(CampaignChannel.SMS);
    }

    @Test
    void getEmailStats_PassesChannelToService() {
        EmailStatsDto dto = EmailStatsDto.builder().totalEmailsSent(100).build();
        when(dashboardService.getEmailStats(CampaignChannel.VOICE)).thenReturn(dto);

        ResponseEntity<ApiResponseDto<EmailStatsDto>> response =
                dashboardController.getEmailStats(CampaignChannel.VOICE);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dashboardService).getEmailStats(CampaignChannel.VOICE);
    }

    @Test
    void getBreachSummary_PassesChannelToService() {
        BreachSummaryDto dto = BreachSummaryDto.builder().totalBreaches(0).build();
        when(dashboardService.getBreachSummary(CampaignChannel.EMAIL)).thenReturn(dto);

        ResponseEntity<ApiResponseDto<BreachSummaryDto>> response =
                dashboardController.getBreachSummary(CampaignChannel.EMAIL);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(dashboardService).getBreachSummary(CampaignChannel.EMAIL);
    }
}
