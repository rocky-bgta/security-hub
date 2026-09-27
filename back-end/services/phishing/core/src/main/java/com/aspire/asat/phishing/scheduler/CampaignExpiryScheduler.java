package com.aspire.asat.phishing.scheduler;

import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignExpiryScheduler {

    private final CampaignRepository campaignRepository;

    @Scheduled(fixedDelay = 1200000)
    public void markExpiredRunningOrPausedCampaigns() {
        Instant now = Instant.now();
        List<Campaign> expiredCampaigns = campaignRepository.findRunningOrPausedCampaignsExpiredAtOrBefore(now);
        if (expiredCampaigns.isEmpty()) {
            return;
        }

        for (Campaign campaign : expiredCampaigns) {
            campaign.setStatus(CampaignStatus.EXPIRED);
            if (campaign.getCompletedAt() == null) {
                campaign.setCompletedAt(now);
            }
        }
        campaignRepository.saveAll(expiredCampaigns);
        log.info("Marked {} running/paused campaigns as EXPIRED", expiredCampaigns.size());
    }
}
