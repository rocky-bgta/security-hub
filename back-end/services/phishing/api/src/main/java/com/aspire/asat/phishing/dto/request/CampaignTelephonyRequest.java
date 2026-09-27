package com.aspire.asat.phishing.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignTelephonyRequest {

    @NotBlank(message = "Voice server configuration id is required")
    private String voiceServerConfigurationId;

    private String region;

    private String countryCode;

    @Valid
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
