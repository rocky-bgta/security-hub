package com.aspire.asat.phishing.delivery;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.model.Campaign;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CampaignSenderResolver {

    private final List<CampaignSender> senders;

    public CampaignSender resolve(Campaign campaign) {
        CampaignChannel channel = campaign.getChannel() != null ? campaign.getChannel() : CampaignChannel.EMAIL;
        Map<CampaignChannel, CampaignSender> senderMap = new EnumMap<>(CampaignChannel.class);
        for (CampaignSender sender : senders) {
            senderMap.put(sender.getChannel(), sender);
        }
        CampaignSender sender = senderMap.get(channel);
        if (sender == null) {
            throw new PhishingValidationException("No campaign sender configured for channel: " + channel);
        }
        return sender;
    }
}
