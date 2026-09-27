package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignVoiceSetupRequest {

    @NotNull(message = "Consent confirmation is required")
    private Boolean consentConfirmed;

    private String consentText;

    private String callerId;
}
