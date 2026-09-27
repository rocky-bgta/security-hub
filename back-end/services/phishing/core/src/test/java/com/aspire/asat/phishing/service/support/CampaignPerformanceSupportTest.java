package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.response.CampaignPerformanceDto;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignStats;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CampaignPerformanceSupportTest {

    @Test
    void toPerformanceDtoShouldUseEmailCounters() {
        Campaign campaign = Campaign.builder()
                .id("c-email")
                .campaignName("Email Camp")
                .status(CampaignStatus.RUNNING)
                .channel(CampaignChannel.EMAIL)
                .stats(CampaignStats.builder()
                        .totalRecipients(100)
                        .emailsSent(90)
                        .emailsDelivered(80)
                        .emailsOpened(40)
                        .linksClicked(10)
                        .dataSubmitted(2)
                        .emailsReported(5)
                        .emailsBounced(3)
                        .build())
                .build();

        CampaignPerformanceDto dto = CampaignPerformanceSupport.toPerformanceDto(campaign);

        assertEquals(90, dto.getEmailsSent());
        assertEquals(80, dto.getEmailsDelivered());
        assertEquals(40, dto.getOpened());
        assertEquals(10, dto.getClicked());
        assertEquals(2, dto.getDataSubmitted());
        assertEquals(5, dto.getReported());
        assertEquals(CampaignChannel.EMAIL, dto.getChannel());
        assertEquals(40.0, dto.getOpenRate());
        assertEquals(10.0, dto.getClickRate());
        assertEquals(2.0, dto.getCompromiseRate());
        assertEquals(5.0, dto.getReportRate());
        assertEquals(88.9, dto.getDeliveryRate());
    }

    @Test
    void toPerformanceDtoShouldMapVoiceCountersInsteadOfEmail() {
        Campaign campaign = Campaign.builder()
                .id("c-voice")
                .campaignName("Voice Camp")
                .status(CampaignStatus.RUNNING)
                .channel(CampaignChannel.VOICE)
                .stats(CampaignStats.builder()
                        .totalRecipients(50)
                        .emailsSent(99)
                        .callsTotal(40)
                        .callsAnswered(25)
                        .callsEngaged(12)
                        .callsCompromised(4)
                        .callsReported(3)
                        .callsFailed(5)
                        .build())
                .build();

        CampaignPerformanceDto dto = CampaignPerformanceSupport.toPerformanceDto(campaign);

        assertEquals(40, dto.getEmailsSent());
        assertEquals(25, dto.getEmailsDelivered());
        assertEquals(25, dto.getOpened());
        assertEquals(12, dto.getClicked());
        assertEquals(4, dto.getDataSubmitted());
        assertEquals(3, dto.getReported());
        assertEquals(5, dto.getBounced());
        assertEquals(CampaignChannel.VOICE, dto.getChannel());
        assertEquals(8.0, dto.getCompromiseRate());
        assertEquals(62.5, dto.getOpenRate());
    }

    @Test
    void toPerformanceDtoShouldMapSmsCountersWithoutOpenOrReport() {
        Campaign campaign = Campaign.builder()
                .id("c-sms")
                .campaignName("SMS Camp")
                .status(CampaignStatus.RUNNING)
                .channel(CampaignChannel.SMS)
                .stats(CampaignStats.builder()
                        .totalRecipients(30)
                        .smsSent(28)
                        .smsDelivered(25)
                        .smsFailed(2)
                        .linksClicked(8)
                        .dataSubmitted(3)
                        .emailsOpened(20)
                        .emailsReported(9)
                        .build())
                .build();

        CampaignPerformanceDto dto = CampaignPerformanceSupport.toPerformanceDto(campaign);

        assertEquals(28, dto.getEmailsSent());
        assertEquals(25, dto.getEmailsDelivered());
        assertEquals(0, dto.getOpened());
        assertEquals(8, dto.getClicked());
        assertEquals(3, dto.getDataSubmitted());
        assertEquals(0, dto.getReported());
        assertEquals(2, dto.getBounced());
        assertEquals(CampaignChannel.SMS, dto.getChannel());
    }
}
