package com.aspire.asat.phishing.scheduler;

import com.aspire.asat.phishing.dto.enums.CampaignStatus;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.delivery.CampaignDeliveryOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Promotes SCHEDULED campaigns to RUNNING when {@code schedule.startDateTime} is reached,
 * then enqueues campaign emails.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CampaignLaunchScheduler {

    private final CampaignRepository campaignRepository;
    private final CampaignDeliveryOrchestrator campaignDeliveryOrchestrator;

    @Scheduled(fixedDelayString = "${campaign.schedule.launch-check-ms:60000}")
    public void launchScheduledCampaignsReadyToStart() {
        Instant now = Instant.now();
        List<Campaign> readyToLaunch = campaignRepository.findScheduledCampaignsReadyToLaunch(now);
        if (readyToLaunch.isEmpty()) {
            return;
        }

        for (Campaign campaign : readyToLaunch) {
            try {
                launchSingleCampaign(campaign, now);
            } catch (Exception ex) {
                log.error("Failed to launch scheduled campaign {}", campaign.getId(), ex);
            }
        }
    }

    private void launchSingleCampaign(Campaign campaign, Instant now) {
        if (campaign.isExpiredAt(now)) {
            campaign.setStatus(CampaignStatus.EXPIRED);
            if (campaign.getCompletedAt() == null) {
                campaign.setCompletedAt(now);
            }
            campaignRepository.save(campaign);
            log.info("Marked scheduled campaign {} as EXPIRED before launch", campaign.getId());
            return;
        }

        campaign.setStatus(CampaignStatus.RUNNING);
        campaign.setLaunchedAt(now);
        campaignRepository.save(campaign);

        campaignDeliveryOrchestrator.publishCampaign(campaign.getId());
        log.info("Launched scheduled campaign {} at {}", campaign.getId(), now);
    }
}
