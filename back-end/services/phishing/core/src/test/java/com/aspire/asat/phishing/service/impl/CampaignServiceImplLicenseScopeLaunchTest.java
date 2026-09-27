package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.delivery.CampaignDeliveryOrchestrator;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.mapper.CampaignMapper;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignAudience;
import com.aspire.asat.phishing.model.CampaignSchedule;
import com.aspire.asat.phishing.model.CampaignStats;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplLicenseScopeLaunchTest {

    @Mock private CampaignRepository campaignRepository;
    @Mock private CampaignRecipientRepository recipientRepository;
    @Mock private CampaignMapper campaignMapper;
    @Mock private UserCurrentContextService userCurrentContextService;
    @Mock private CampaignDeliveryOrchestrator campaignDeliveryOrchestrator;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    @Test
    void launchCampaign_withoutProductPackageId_throwsValidation() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().clientAdminId("client-1").build());

        Campaign campaign = Campaign.builder()
                .id("cmp-1")
                .clientId("client-1")
                .campaignName("Test")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .channel(CampaignChannel.EMAIL)
                .status(CampaignStatus.DRAFT)
                .currentStep(9)
                .senderProfileId("sp-1")
                .audience(CampaignAudience.builder().recipientCount(1).build())
                .schedule(CampaignSchedule.builder().build())
                .stats(new CampaignStats())
                .build();
        when(campaignRepository.findByIdAndClientId("cmp-1", "client-1")).thenReturn(Optional.of(campaign));

        PhishingValidationException ex = assertThrows(PhishingValidationException.class,
                () -> campaignService.launchCampaign("cmp-1"));

        assertTrue(ex.getMessage().contains("productPackageId"));
        verify(campaignDeliveryOrchestrator, never()).publishCampaign(any());
        verify(recipientRepository, never()).findByCampaignId(any());
    }
}
