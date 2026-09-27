package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.model.CampaignRecipient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TemplatePersonalizationServiceTest {

    private final TemplatePersonalizationService service = new TemplatePersonalizationService();

    @Test
    void personalize_replacesCanonicalRecipientTokens() {
        CampaignRecipient recipient = CampaignRecipient.builder()
                .firstName("Ada")
                .lastName("Lovelace")
                .email("ada@example.com")
                .department("Engineering")
                .organizationName("Aspire")
                .organizationDomain("aspire.com")
                .phoneNumber("+15551234567")
                .countryName("United States")
                .trackingId("trk-1")
                .build();

        String content = "Hi {{FIRST_NAME}} {{LAST_NAME}} ({{FULL_NAME}}). "
                + "Email {{EMAIL_ADDRESS}}, dept {{DEPARTMENT}}, org {{organizationName}} "
                + "({{organizationDomain}}/{{domain}}), phone {{PHONE_NUMBER}}, "
                + "location {{LOCATION}}, track {{TRACKING_ID}}.";

        String rendered = service.personalize(content, recipient);

        assertEquals("Hi Ada Lovelace (Ada Lovelace). "
                + "Email ada@example.com, dept Engineering, org Aspire "
                + "(aspire.com/aspire.com), phone +15551234567, "
                + "location United States, track trk-1.", rendered);
    }

    @Test
    void personalize_replacesLegacyVishingEmailAndPhoneAliases() {
        CampaignRecipient recipient = CampaignRecipient.builder()
                .email("legacy@example.com")
                .phoneNumber("+15559876543")
                .build();

        String rendered = service.personalize(
                "Reach {{EMAIL}} at {{PHONE}}", recipient);

        assertEquals("Reach legacy@example.com at +15559876543", rendered);
    }

    @Test
    void personalize_nullContent_returnsNull() {
        assertNull(service.personalize(null, CampaignRecipient.builder().build()));
    }

    @Test
    void personalize_nullRecipient_returnsOriginalContent() {
        assertEquals("{{FIRST_NAME}}", service.personalize("{{FIRST_NAME}}", null));
    }

    @Test
    void personalize_nullFields_becomeEmptyStrings() {
        assertEquals("Hi  ", service.personalize("Hi {{FIRST_NAME}} {{LAST_NAME}}",
                CampaignRecipient.builder().build()));
    }
}
