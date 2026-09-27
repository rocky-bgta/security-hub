package com.aspire.asat.phishing.dto.request;

import com.aspire.asat.phishing.dto.response.BrandDto;
import com.aspire.asat.phishing.dto.response.CallToActionDto;
import com.aspire.asat.phishing.dto.response.ConstraintsDataDto;
import com.aspire.asat.phishing.dto.response.EmotionalTriggerDto;
import com.aspire.asat.phishing.dto.response.ToneDto;
import com.aspire.asat.phishing.dto.response.UrgencyLevelDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Optional generation options that are not already declared on
 * {@link AITemplateGenerateRequest} or {@link AILandingPageRequest}.
 * Language, difficulty, generation mode, and layout style belong on those parent requests only.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGenerationOptionsRequest {

    private ToneDto tone;
    private BrandDto brand;
    private String contentLength;
    private CallToActionDto callToAction;
    private UrgencyLevelDto urgencyLevel;
    private EmotionalTriggerDto emotionalTrigger;
    @Builder.Default
    private String language = "English";
    private ConstraintsDataDto constraints;

}
