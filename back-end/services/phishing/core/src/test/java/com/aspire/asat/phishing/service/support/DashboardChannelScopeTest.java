package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.dto.enums.ActivityType;
import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.CampaignType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DashboardChannelScopeTest {

    @Test
    void effectiveDefaultsNullToEmail() {
        assertEquals(CampaignChannel.EMAIL, DashboardChannelScope.effective(null));
        assertEquals(CampaignChannel.SMS, DashboardChannelScope.effective(CampaignChannel.SMS));
    }

    @Test
    void campaignTypesMatchChannelProducts() {
        assertTrue(DashboardChannelScope.campaignTypes(null).contains(CampaignType.SIMULATED_PHISHING));
        assertTrue(DashboardChannelScope.campaignTypes(CampaignChannel.EMAIL)
                .contains(CampaignType.PHISHING_WITH_TRAINING));
        assertFalse(DashboardChannelScope.campaignTypes(CampaignChannel.EMAIL)
                .contains(CampaignType.SMISHING_SIMULATION));
        assertTrue(DashboardChannelScope.campaignTypes(CampaignChannel.SMS)
                .contains(CampaignType.SMISHING_WITH_TRAINING));
        assertTrue(DashboardChannelScope.campaignTypes(CampaignChannel.VOICE)
                .contains(CampaignType.VISHING_SIMULATION));
    }

    @Test
    void activityMappingUsesChannelSpecificSentAndHackTypes() {
        DashboardChannelScope.ActivityMapping email = DashboardChannelScope.activityMapping(null);
        assertEquals(ActivityType.EMAIL_SENT, email.sent());
        assertEquals(ActivityType.DATA_SUBMITTED, email.hack());
        assertEquals(ActivityType.EMAIL_REPORTED, email.reported());

        DashboardChannelScope.ActivityMapping sms = DashboardChannelScope.activityMapping(CampaignChannel.SMS);
        assertEquals(ActivityType.SMS_SENT, sms.sent());
        assertNull(sms.opened());
        assertEquals(ActivityType.LINK_CLICKED, sms.clicked());
        assertEquals(ActivityType.DATA_SUBMITTED, sms.hack());
        assertNull(sms.reported());

        DashboardChannelScope.ActivityMapping voice = DashboardChannelScope.activityMapping(CampaignChannel.VOICE);
        assertEquals(ActivityType.VOICE_INITIATED, voice.sent());
        assertEquals(ActivityType.VOICE_COMPROMISED, voice.hack());
        assertEquals(ActivityType.VOICE_REPORTED, voice.reported());
    }
}
