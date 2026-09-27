package com.aspire.asat.phishing.delivery;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.service.CampaignEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailCampaignSender implements CampaignSender {

    private final CampaignEmailService campaignEmailService;

    @Override
    public CampaignChannel getChannel() {
        return CampaignChannel.EMAIL;
    }

    @Override
    public void publish(Campaign campaign) {
        log.info("Publishing email campaign: {}", campaign.getId());
        campaignEmailService.publishCampaignEmails(campaign.getId());
    }
}
