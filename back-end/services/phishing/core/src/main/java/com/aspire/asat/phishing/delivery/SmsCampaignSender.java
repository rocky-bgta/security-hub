package com.aspire.asat.phishing.delivery;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.service.CampaignSmsPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsCampaignSender implements CampaignSender {

    private final CampaignSmsPublishService campaignSmsPublishService;

    @Override
    public CampaignChannel getChannel() {
        return CampaignChannel.SMS;
    }

    @Override
    public void publish(Campaign campaign) {
        log.info("Publishing SMS campaign: {}", campaign.getId());
        campaignSmsPublishService.publishCampaignSms(campaign.getId());
    }
}
