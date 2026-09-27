package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class CampaignExpiryTest {

    @Test
    void isExpiredAtShouldReturnTrueWhenExpiresAtInPast() {
        Campaign campaign = Campaign.builder()
                .campaignName("Test Campaign")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .status(CampaignStatus.RUNNING)
                .currentStep(9)
                .expiresAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();

        boolean expired = campaign.isExpiredAt(Instant.parse("2026-01-01T00:00:01Z"));

        Assertions.assertTrue(expired);
    }

    @Test
    void canLaunchShouldBeFalseWhenCampaignExpired() {
        Campaign campaign = Campaign.builder()
                .campaignName("Test Campaign")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .status(CampaignStatus.DRAFT)
                .currentStep(9)
                .expiresAt(Instant.now().minusSeconds(30))
                .build();

        Assertions.assertFalse(campaign.canLaunch());
    }

    @Test
    void canPauseResumeCancelShouldBeFalseWhenExpired() {
        Instant expiredAt = Instant.now().minusSeconds(60);

        Campaign running = Campaign.builder()
                .campaignName("Test Campaign")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .status(CampaignStatus.RUNNING)
                .currentStep(9)
                .expiresAt(expiredAt)
                .build();
        Campaign paused = Campaign.builder()
                .campaignName("Test Campaign")
                .campaignType(CampaignType.SIMULATED_PHISHING)
                .status(CampaignStatus.PAUSED)
                .currentStep(9)
                .expiresAt(expiredAt)
                .build();

        Assertions.assertFalse(running.canPause());
        Assertions.assertFalse(running.canCancel());
        Assertions.assertFalse(paused.canResume());
    }
}
