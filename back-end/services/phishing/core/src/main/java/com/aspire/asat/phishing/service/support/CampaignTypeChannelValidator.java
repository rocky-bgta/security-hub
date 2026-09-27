package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import com.aspire.asat.phishing.exception.PhishingValidationException;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Validates campaign type is allowed for the selected delivery channel.
 */
public final class CampaignTypeChannelValidator {

    private static final Map<CampaignChannel, Set<CampaignType>> ALLOWED_TYPES = Map.of(
            CampaignChannel.EMAIL, EnumSet.of(
                    CampaignType.SIMULATED_PHISHING,
                    CampaignType.PHISHING_WITH_TRAINING
            ),
            CampaignChannel.SMS, EnumSet.of(
                    CampaignType.SMISHING_SIMULATION,
                    CampaignType.SMISHING_WITH_TRAINING
            ),
            CampaignChannel.VOICE, EnumSet.of(
                    CampaignType.VISHING_SIMULATION,
                    CampaignType.VISHING_WITH_TRAINING
            )
    );

    private CampaignTypeChannelValidator() {
    }

    public static CampaignChannel effectiveChannel(CampaignChannel channel) {
        return channel != null ? channel : CampaignChannel.EMAIL;
    }

    public static void validate(CampaignChannel channel, CampaignType campaignType) {
        if (campaignType == null) {
            throw new PhishingValidationException("Campaign type is required");
        }
        CampaignChannel effectiveChannel = effectiveChannel(channel);
        Set<CampaignType> allowed = ALLOWED_TYPES.get(effectiveChannel);
        if (allowed == null || !allowed.contains(campaignType)) {
            throw new PhishingValidationException(
                    "Campaign type " + campaignType + " is not allowed for channel " + effectiveChannel);
        }
    }

    public static boolean isAllowed(CampaignChannel channel, CampaignType campaignType) {
        if (campaignType == null) {
            return false;
        }
        Set<CampaignType> allowed = ALLOWED_TYPES.get(effectiveChannel(channel));
        return allowed != null && allowed.contains(campaignType);
    }
}
