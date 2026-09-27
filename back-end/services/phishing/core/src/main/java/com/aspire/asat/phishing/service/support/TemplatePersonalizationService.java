package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.model.CampaignRecipient;
import org.springframework.stereotype.Component;

/**
 * Replaces template placeholders with recipient-specific values.
 */
@Component
public class TemplatePersonalizationService {

    public String personalize(String content, CampaignRecipient recipient) {
        if (content == null || recipient == null) {
            return content;
        }
        String result = content;
        result = result.replace("{{FIRST_NAME}}", nullToEmpty(recipient.getFirstName()));
        result = result.replace("{{LAST_NAME}}", nullToEmpty(recipient.getLastName()));
        result = result.replace("{{FULL_NAME}}", nullToEmpty(recipient.getFullName()));
        result = result.replace("{{EMAIL_ADDRESS}}", nullToEmpty(recipient.getEmail()));
        // Legacy vishing alias (same value as EMAIL_ADDRESS)
        result = result.replace("{{EMAIL}}", nullToEmpty(recipient.getEmail()));
        result = result.replace("{{DEPARTMENT}}", nullToEmpty(recipient.getDepartment()));
        result = result.replace("{{organizationName}}", nullToEmpty(recipient.getOrganizationName()));
        result = result.replace("{{organizationDomain}}", nullToEmpty(recipient.getOrganizationDomain()));
        result = result.replace("{{domain}}", nullToEmpty(recipient.getOrganizationDomain()));
        result = result.replace("{{PHONE_NUMBER}}", nullToEmpty(recipient.getPhoneNumber()));
        // Legacy vishing alias (same value as PHONE_NUMBER)
        result = result.replace("{{PHONE}}", nullToEmpty(recipient.getPhoneNumber()));
        result = result.replace("{{LOCATION}}", nullToEmpty(recipient.getCountryName()));
        result = result.replace("{{TRACKING_ID}}", nullToEmpty(recipient.getTrackingId()));
        return result;
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}
