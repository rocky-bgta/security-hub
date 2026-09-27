package com.aspire.asat.phishing.delivery;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.model.Campaign;

public interface CampaignSender {

    CampaignChannel getChannel();

    void publish(Campaign campaign);
}
