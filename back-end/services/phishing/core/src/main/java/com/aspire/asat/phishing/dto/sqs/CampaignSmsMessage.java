package com.aspire.asat.phishing.dto.sqs;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignSmsMessage {

    @Builder.Default
    private CampaignChannel channel = CampaignChannel.SMS;

    private String campaignId;
    private String recipientId;
    private String clientId;
    private String trackingId;
    private String toPhone;
    private String messageBody;
    private String smsServerConfigurationId;
    private String campaignName;
}
