package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.common.message.ToastMessageResolver;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.response.UserCampaignStatisticsDto;
import com.aspire.asat.phishing.service.CampaignService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignControllerImplUserStatisticsTest {

    @Mock
    private CampaignService campaignService;
    @Mock
    private MessageService messageService;
    @Mock
    private ToastMessageResolver toastMessageResolver;

    @InjectMocks
    private CampaignControllerImpl controller;

    @Test
    void getUserCampaignStatistics_forwardsChannelToService() {
        UserCampaignStatisticsDto stats = UserCampaignStatisticsDto.builder()
                .totalCampaigns(2L)
                .openCount(1L)
                .build();
        when(campaignService.getUserCampaignStatistics(CampaignChannel.SMS)).thenReturn(stats);

        ResponseEntity<ApiResponseDto<UserCampaignStatisticsDto>> response =
                controller.getUserCampaignStatistics(CampaignChannel.SMS);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2L, response.getBody().getData().getTotalCampaigns());
        verify(campaignService).getUserCampaignStatistics(CampaignChannel.SMS);
    }

    @Test
    void getUserCampaignStatistics_forwardsEmailChannel() {
        when(campaignService.getUserCampaignStatistics(CampaignChannel.EMAIL))
                .thenReturn(UserCampaignStatisticsDto.builder().totalCampaigns(1L).build());

        controller.getUserCampaignStatistics(CampaignChannel.EMAIL);

        verify(campaignService).getUserCampaignStatistics(CampaignChannel.EMAIL);
    }
}
