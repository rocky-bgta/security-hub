package com.aspire.asat.phishing.service.support;

import com.aspire.asat.common.util.PhoneNumberUtils;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.model.CampaignRecipient;
import com.aspire.asat.phishing.model.EmailTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Validates SMS template content and rendered length after placeholder replacement.
 */
@Component
public class SmsTemplateValidator {

    public static final int SMS_MAX_LENGTH = 160;
    public static final int SMS_RECOMMENDED_LENGTH = 120;

    private final TemplatePersonalizationService personalizationService;

    public SmsTemplateValidator(TemplatePersonalizationService personalizationService) {
        this.personalizationService = personalizationService;
    }

    public void validateSmsBody(String smsBody) {
        if (smsBody == null || smsBody.isBlank()) {
            throw new PhishingValidationException("SMS body is required");
        }
        if (PhoneNumberUtils.containsHtml(smsBody)) {
            throw new PhishingValidationException("SMS templates must be plain text only; HTML is not allowed");
        }
    }

    public void validateRenderedLength(String smsBody, String trackingUrl, CampaignRecipient sampleRecipient) {
        validateSmsBody(smsBody);
        String rendered = renderSmsBody(smsBody, trackingUrl, sampleRecipient);
        if (rendered.length() > SMS_MAX_LENGTH) {
            throw new PhishingValidationException(
                    "Rendered SMS exceeds " + SMS_MAX_LENGTH + " characters (actual: " + rendered.length() + ")");
        }
    }

    public String renderSmsBody(String smsBody, String trackingUrl, CampaignRecipient recipient) {
        String withTracking = smsBody
                .replace("{{tracking_link}}", trackingUrl)
                .replace("{{TRACKING_LINK}}", trackingUrl)
                .replace("{{PHISHING_URL}}", trackingUrl)
                .replace("{{PHISHING_LINK}}", trackingUrl);
        return personalizationService.personalize(withTracking, recipient);
    }

    public Map<String, Object> preview(String smsBody, String trackingUrl, CampaignRecipient sampleRecipient) {
        String rendered = renderSmsBody(smsBody, trackingUrl, sampleRecipient);
        return Map.of(
                "renderedBody", rendered,
                "length", rendered.length(),
                "withinLimit", rendered.length() <= SMS_MAX_LENGTH,
                "recommendedLimit", SMS_RECOMMENDED_LENGTH,
                "maxLimit", SMS_MAX_LENGTH
        );
    }

    public void validateTemplate(EmailTemplate template, String trackingUrl, CampaignRecipient sampleRecipient) {
        if (template.getTemplateType() != com.aspire.asat.phishing.dto.enums.TemplateType.SMS) {
            return;
        }
        validateRenderedLength(template.getSmsBody(), trackingUrl, sampleRecipient);
    }
}
