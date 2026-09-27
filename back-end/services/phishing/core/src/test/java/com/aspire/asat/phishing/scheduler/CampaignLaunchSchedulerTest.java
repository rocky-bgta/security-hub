package com.aspire.asat.phishing.scheduler;

import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.dto.enums.ScheduleType;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignSchedule;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.delivery.CampaignDeliveryOrchestrator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignLaunchSchedulerTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private CampaignDeliveryOrchestrator campaignDeliveryOrchestrator;

    @InjectMocks
    private CampaignLaunchScheduler scheduler;

    @Test
    void launchScheduledCampaignsReadyToStartPromotesToRunningAndPublishesEmails() {
        Instant start = Instant.now().minusSeconds(60);
        Campaign scheduled = Campaign.builder()
                .id("cmp-scheduled")
                .campaignName("Scheduled")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .status(CampaignStatus.SCHEDULED)
                .schedule(CampaignSchedule.builder()
                        .type(ScheduleType.SCHEDULED)
                        .startDateTime(start)
                        .timeZone("EAT (UTC+03:00)")
                        .build())
                .build();

        when(campaignRepository.findScheduledCampaignsReadyToLaunch(any()))
                .thenReturn(List.of(scheduled));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.launchScheduledCampaignsReadyToStart();

        ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
        verify(campaignRepository).save(captor.capture());
        assertEquals(CampaignStatus.RUNNING, captor.getValue().getStatus());
        assertNotNull(captor.getValue().getLaunchedAt());
        verify(campaignDeliveryOrchestrator).publishCampaign(eq("cmp-scheduled"));
    }

    @Test
    void launchScheduledCampaignsSkipsSaveWhenNoneReady() {
        when(campaignRepository.findScheduledCampaignsReadyToLaunch(any())).thenReturn(List.of());

        scheduler.launchScheduledCampaignsReadyToStart();

        verify(campaignRepository, never()).save(any());
        verify(campaignDeliveryOrchestrator, never()).publishCampaign(any());
    }

    @Test
    void launchScheduledCampaignsMarksExpiredBeforeStartAsExpired() {
        Campaign expiredScheduled = Campaign.builder()
                .id("cmp-expired")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .status(CampaignStatus.SCHEDULED)
                .expiresAt(Instant.now().minusSeconds(30))
                .schedule(CampaignSchedule.builder()
                        .type(ScheduleType.SCHEDULED)
                        .startDateTime(Instant.now().minusSeconds(120))
                        .build())
                .build();

        when(campaignRepository.findScheduledCampaignsReadyToLaunch(any()))
                .thenReturn(List.of(expiredScheduled));
        when(campaignRepository.save(any(Campaign.class))).thenAnswer(inv -> inv.getArgument(0));

        scheduler.launchScheduledCampaignsReadyToStart();

        ArgumentCaptor<Campaign> captor = ArgumentCaptor.forClass(Campaign.class);
        verify(campaignRepository).save(captor.capture());
        assertEquals(CampaignStatus.EXPIRED, captor.getValue().getStatus());
        verify(campaignDeliveryOrchestrator, never()).publishCampaign(any());
    }
}
