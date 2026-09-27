package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;

import java.util.List;

public interface RecipientResolver {

    CampaignChannel getChannel();

    List<RegistrationServiceClient.UserDto> filterEligibleUsers(
            List<RegistrationServiceClient.UserDto> users);

    void validateBeforeLaunch(Campaign campaign, List<CampaignRecipient> recipients);
}
