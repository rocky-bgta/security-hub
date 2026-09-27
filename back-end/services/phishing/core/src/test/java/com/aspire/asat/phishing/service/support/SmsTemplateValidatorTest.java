package com.aspire.asat.phishing.service.support;

import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.model.CampaignRecipient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmsTemplateValidatorTest {

    private SmsTemplateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SmsTemplateValidator(new TemplatePersonalizationService());
    }

    @Test
    void rejectsHtmlInSmsBody() {
        assertThrows(PhishingValidationException.class,
                () -> validator.validateSmsBody("Click <a href='x'>here</a>"));
    }

    @Test
    void rejectsRenderedLengthOver160() {
        String longBody = "A".repeat(140);
        String trackingUrl = "https://example.com/t/phish/abc123";
        CampaignRecipient recipient = CampaignRecipient.builder().trackingId("abc123").build();
        assertThrows(PhishingValidationException.class,
                () -> validator.validateRenderedLength(longBody + " {{tracking_link}}", trackingUrl, recipient));
    }

    @Test
    void acceptsValidRenderedSms() {
        String body = "Verify now: {{tracking_link}}";
        String trackingUrl = "https://ex.com/t/phish/id";
        CampaignRecipient recipient = CampaignRecipient.builder().firstName("Sam").trackingId("id").build();
        String rendered = validator.renderSmsBody(body, trackingUrl, recipient);
        assertTrue(rendered.contains(trackingUrl));
        assertEquals(rendered.length(), validator.preview(body, trackingUrl, recipient).get("length"));
    }
}
