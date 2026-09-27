package com.aspire.asat.phishing.delivery;

import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampaignDeliveryOrchestratorImpl implements CampaignDeliveryOrchestrator {

    private final CampaignRepository campaignRepository;
    private final CampaignSenderResolver campaignSenderResolver;

    @Override
    public void publishCampaign(String campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + campaignId));
        CampaignSender sender = campaignSenderResolver.resolve(campaign);
        sender.publish(campaign);
    }
}
