package com.aspire.asat.phishing.dto.sqs;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.VoiceCloneProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignVoiceMessage {

    @Builder.Default
    private CampaignChannel channel = CampaignChannel.VOICE;

    private String campaignId;
    private String recipientId;
    private String clientId;
    private String trackingId;
    private String toPhone;
    private String renderedScript;
    private String voiceServerConfigurationId;
    private String externalVoiceId;
    private VoiceCloneProvider voiceCloneProvider;
    private String language;
    private String callerId;
    private String campaignName;
    private int maxRetries;
    private int retryIntervalMinutes;
}
