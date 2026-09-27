package com.aspire.asat.phishing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignTelephonyData {

    private String voiceServerConfigurationId;
    private String region;
    private String countryCode;
    @Builder.Default
    private RetryPolicy retryPolicy = new RetryPolicy();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryPolicy {
        @Builder.Default
        private int maxRetries = 3;
        @Builder.Default
        private int intervalMinutes = 15;
    }
}
