package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.common.message.ToastMessageResolver;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.CampaignExpiryValidityUnit;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.request.CampaignCreateRequest;
import com.aspire.asat.phishing.dto.request.CampaignExpireDateRequest;
import com.aspire.asat.phishing.dto.response.CampaignDto;
import com.aspire.asat.phishing.service.CampaignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignControllerImplExpireDateTest {

    private static final String CAMPAIGN_SAVED_DRAFT_MESSAGE =
            "The campaign has been saved as a draft successfully. You can continue editing configurations before launching or scheduling the campaign.";

    @Mock
    private CampaignService campaignService;

    @Mock
    private MessageService messageService;

    @Mock
    private ToastMessageResolver toastMessageResolver;

    @InjectMocks
    private CampaignControllerImpl campaignController;

    @BeforeEach
    void setUp() {
        lenient().when(messageService.get(MessageKeys.CAMPAIGN_SAVED_DRAFT)).thenReturn(CAMPAIGN_SAVED_DRAFT_MESSAGE);
    }

    @Test
    void createCampaignShouldAcceptExpireDateRequestShape() {
        CampaignCreateRequest request = CampaignCreateRequest.builder()
                .campaignName("Q2 Finance")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .expireDate(CampaignExpireDateRequest.builder()
                        .validityUnit(CampaignExpiryValidityUnit.DAYS)
                        .validityPeriod(31)
                        .build())
                .build();

        when(campaignService.createCampaign(request)).thenReturn(CampaignDto.builder().campaignId("cmp-1").build());
        ResponseEntity<ApiResponseDto<CampaignDto>> response = campaignController.createCampaign(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(CAMPAIGN_SAVED_DRAFT_MESSAGE, response.getBody().getMessage());
    }

    @Test
    void updateStep1ShouldAcceptExpireDateRequestShape() {
        CampaignCreateRequest request = CampaignCreateRequest.builder()
                .campaignName("Q3 Finance")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .expireDate(CampaignExpireDateRequest.builder()
                        .validityUnit(CampaignExpiryValidityUnit.MONTHS)
                        .validityPeriod(12)
                        .build())
                .build();

        when(campaignService.updateCampaignSetup("cmp-1", request))
                .thenReturn(CampaignDto.builder().campaignId("cmp-1").build());
        ResponseEntity<ApiResponseDto<CampaignDto>> response =
                campaignController.updateCampaignSetup("cmp-1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Campaign setup updated successfully", response.getBody().getMessage());
    }
}
