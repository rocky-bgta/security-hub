package com.aspire.asat.phishing.service.support;

import com.aspire.asat.common.util.PhoneNumberUtils;
import com.aspire.asat.phishing.client.RegistrationServiceClient;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.model.Campaign;
import com.aspire.asat.phishing.model.CampaignRecipient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SmsRecipientResolver implements RecipientResolver {

    @Override
    public CampaignChannel getChannel() {
        return CampaignChannel.SMS;
    }

    @Override
    public List<RegistrationServiceClient.UserDto> filterEligibleUsers(
            List<RegistrationServiceClient.UserDto> users) {
        return users.stream()
                .filter(user -> PhoneNumberUtils.isValidE164(PhoneNumberUtils.normalize(user.getPhoneNumber())))
                .collect(Collectors.toList());
    }

    @Override
    public void validateBeforeLaunch(Campaign campaign, List<CampaignRecipient> recipients) {
        if (recipients == null || recipients.isEmpty()) {
            throw new PhishingValidationException("Campaign has no eligible SMS recipients with valid mobile numbers");
        }
        long invalid = recipients.stream()
                .filter(r -> !PhoneNumberUtils.isValidE164(PhoneNumberUtils.normalize(r.getPhoneNumber())))
                .count();
        if (invalid > 0) {
            throw new PhishingValidationException("Campaign has " + invalid + " recipient(s) with invalid mobile numbers");
        }
    }
}
