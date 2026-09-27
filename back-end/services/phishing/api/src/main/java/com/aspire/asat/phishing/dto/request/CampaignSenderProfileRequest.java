package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Step 4: Sender Profile Selection
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignSenderProfileRequest {

    @NotBlank(message = "Sender profile is required")
    private String senderProfileId;
}
