package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmailRecipientResolver implements RecipientResolver {

    @Override
    public CampaignChannel getChannel() {
        return CampaignChannel.EMAIL;
    }

    @Override
    public List<RegistrationServiceClient.UserDto> filterEligibleUsers(
            List<RegistrationServiceClient.UserDto> users) {
        return users.stream()
                .filter(user -> user.getEmail() != null && !user.getEmail().isEmpty())
                .collect(Collectors.toList());
    }

    @Override
    public void validateBeforeLaunch(Campaign campaign, List<CampaignRecipient> recipients) {
        // Email launch behavior unchanged: no additional recipient validation at launch.
    }
}
