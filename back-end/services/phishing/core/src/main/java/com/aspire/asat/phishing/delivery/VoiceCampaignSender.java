package com.aspire.asat.phishing.delivery;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.service.CampaignVoicePublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VoiceCampaignSender implements CampaignSender {

    private final CampaignVoicePublishService campaignVoicePublishService;

    @Override
    public CampaignChannel getChannel() {
        return CampaignChannel.VOICE;
    }

    @Override
    public void publish(Campaign campaign) {
        log.info("Publishing voice campaign: {}", campaign.getId());
        campaignVoicePublishService.publishCampaignVoice(campaign.getId());
    }
}
