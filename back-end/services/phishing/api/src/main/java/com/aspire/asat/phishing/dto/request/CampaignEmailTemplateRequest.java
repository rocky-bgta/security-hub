package com.aspire.asat.phishing.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Step 2: Email Template Selection
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignEmailTemplateRequest {

    @NotBlank(message = "Email template is required")
    private String emailTemplateId;

    private String templateLanguage;
}
