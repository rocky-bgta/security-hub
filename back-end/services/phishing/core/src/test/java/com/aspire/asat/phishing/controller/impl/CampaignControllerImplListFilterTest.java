package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.common.message.ToastMessageResolver;
import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.response.CampaignDto;
import com.aspire.asat.phishing.service.CampaignService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignControllerImplListFilterTest {

    @Mock
    private CampaignService campaignService;
    @Mock
    private MessageService messageService;
    @Mock
    private ToastMessageResolver toastMessageResolver;

    @InjectMocks
    private CampaignControllerImpl controller;

    @Test
    void getCampaigns_passesSameFiltersToListAndCount() {
        when(campaignService.getCampaigns(
                eq(0), eq(12), isNull(), isNull(), eq(CampaignChannel.VOICE), eq("createdAt"), eq("desc")))
                .thenReturn(List.of(CampaignDto.builder().channel(CampaignChannel.VOICE).build()));
        when(campaignService.countCampaigns(null, null, CampaignChannel.VOICE)).thenReturn(7L);

        ResponseEntity<AllResponseDto<List<CampaignDto>>> response = controller.getCampaigns(
                0, 12, null, null, CampaignChannel.VOICE, "createdAt", "desc", null);

        assertEquals(7L, response.getBody().getTotal());
        assertEquals(1, response.getBody().getItems().size());
        verify(campaignService).getCampaigns(0, 12, null, null, CampaignChannel.VOICE, "createdAt", "desc");
        verify(campaignService).countCampaigns(null, null, CampaignChannel.VOICE);
    }

    @Test
    void getCampaigns_prefersSortDirectionAlias() {
        when(campaignService.getCampaigns(
                eq(0), eq(12), eq("phish"), eq(CampaignStatus.RUNNING), eq(CampaignChannel.SMS),
                eq("createdAt"), eq("asc")))
                .thenReturn(List.of());
        when(campaignService.countCampaigns("phish", CampaignStatus.RUNNING, CampaignChannel.SMS))
                .thenReturn(0L);

        controller.getCampaigns(
                0, 12, "phish", CampaignStatus.RUNNING, CampaignChannel.SMS, "createdAt", "desc", "asc");

        verify(campaignService).getCampaigns(
                0, 12, "phish", CampaignStatus.RUNNING, CampaignChannel.SMS, "createdAt", "asc");
        verify(campaignService).countCampaigns("phish", CampaignStatus.RUNNING, CampaignChannel.SMS);
    }
}
