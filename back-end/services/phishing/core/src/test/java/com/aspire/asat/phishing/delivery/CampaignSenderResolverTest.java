package com.aspire.asat.phishing.delivery;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.model.Campaign;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignSenderResolverTest {

    @Mock
    private EmailCampaignSender emailCampaignSender;

    @Mock
    private SmsCampaignSender smsCampaignSender;

    @Mock
    private VoiceCampaignSender voiceCampaignSender;

    @InjectMocks
    private CampaignSenderResolver resolver;

    @Test
    void resolvesEmailSender() {
        when(emailCampaignSender.getChannel()).thenReturn(CampaignChannel.EMAIL);
        when(smsCampaignSender.getChannel()).thenReturn(CampaignChannel.SMS);
        when(voiceCampaignSender.getChannel()).thenReturn(CampaignChannel.VOICE);
        CampaignSenderResolver testResolver = new CampaignSenderResolver(
                java.util.List.of(emailCampaignSender, smsCampaignSender, voiceCampaignSender));

        Campaign campaign = Campaign.builder().channel(CampaignChannel.EMAIL).build();
        testResolver.resolve(campaign);
        verify(emailCampaignSender).getChannel();
    }

    @Test
    void resolvesSmsSender() {
        when(emailCampaignSender.getChannel()).thenReturn(CampaignChannel.EMAIL);
        when(smsCampaignSender.getChannel()).thenReturn(CampaignChannel.SMS);
        when(voiceCampaignSender.getChannel()).thenReturn(CampaignChannel.VOICE);
        CampaignSenderResolver testResolver = new CampaignSenderResolver(
                java.util.List.of(emailCampaignSender, smsCampaignSender, voiceCampaignSender));

        Campaign campaign = Campaign.builder().channel(CampaignChannel.SMS).build();
        CampaignSender sender = testResolver.resolve(campaign);
        org.junit.jupiter.api.Assertions.assertEquals(CampaignChannel.SMS, sender.getChannel());
    }

    @Test
    void resolvesVoiceSender() {
        when(emailCampaignSender.getChannel()).thenReturn(CampaignChannel.EMAIL);
        when(smsCampaignSender.getChannel()).thenReturn(CampaignChannel.SMS);
        when(voiceCampaignSender.getChannel()).thenReturn(CampaignChannel.VOICE);
        CampaignSenderResolver testResolver = new CampaignSenderResolver(
                java.util.List.of(emailCampaignSender, smsCampaignSender, voiceCampaignSender));

        Campaign campaign = Campaign.builder().channel(CampaignChannel.VOICE).build();
        CampaignSender sender = testResolver.resolve(campaign);
        org.junit.jupiter.api.Assertions.assertEquals(CampaignChannel.VOICE, sender.getChannel());
    }
}
