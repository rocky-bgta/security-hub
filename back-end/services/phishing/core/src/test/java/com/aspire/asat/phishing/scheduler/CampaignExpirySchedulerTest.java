package com.aspire.asat.phishing.scheduler;

import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.repository.CampaignRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignExpirySchedulerTest {

    @Mock
    private CampaignRepository campaignRepository;

    @InjectMocks
    private CampaignExpiryScheduler scheduler;

    @Test
    void markExpiredRunningOrPausedCampaignsShouldUpdateStatusesToExpired() {
        Campaign runningCampaign = Campaign.builder()
                .id("c1")
                .campaignName("Running Campaign")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .status(CampaignStatus.RUNNING)
                .expiresAt(Instant.now().minusSeconds(60))
                .build();
        Campaign pausedCampaign = Campaign.builder()
                .id("c2")
                .campaignName("Paused Campaign")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .status(CampaignStatus.PAUSED)
                .expiresAt(Instant.now().minusSeconds(120))
                .build();

        when(campaignRepository.findRunningOrPausedCampaignsExpiredAtOrBefore(any()))
                .thenReturn(List.of(runningCampaign, pausedCampaign));

        scheduler.markExpiredRunningOrPausedCampaigns();

        ArgumentCaptor<List<Campaign>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(campaignRepository).saveAll(listCaptor.capture());
        List<Campaign> savedCampaigns = listCaptor.getValue();
        assertEquals(2, savedCampaigns.size());
        assertEquals(CampaignStatus.EXPIRED, savedCampaigns.get(0).getStatus());
        assertEquals(CampaignStatus.EXPIRED, savedCampaigns.get(1).getStatus());
        assertNotNull(savedCampaigns.get(0).getCompletedAt());
        assertNotNull(savedCampaigns.get(1).getCompletedAt());
    }

    @Test
    void markExpiredRunningOrPausedCampaignsShouldSkipSaveWhenNoCampaignMatches() {
        when(campaignRepository.findRunningOrPausedCampaignsExpiredAtOrBefore(any())).thenReturn(List.of());

        scheduler.markExpiredRunningOrPausedCampaigns();

        verify(campaignRepository, never()).saveAll(any());
    }
}
