package com.aspire.asat.phishing.mapper;

import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.dto.response.EmailTemplateDto;
import com.aspire.asat.phishing.dto.response.EmailTemplatePreviewDto;
import com.aspire.asat.phishing.model.EmailTemplate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailTemplateMapperTest {

    private final EmailTemplateMapper mapper = new EmailTemplateMapper();

    @Test
    void toListItemDto_omitsEmailBodyFromPreview() {
        EmailTemplate template = EmailTemplate.builder()
                .id("tpl-1")
                .emailBody("<html>secret</html>")
                .build();

        EmailTemplateDto dto = mapper.toListItemDto(template, true, false);

        assertEquals("", dto.getEmailBodyPreview());
    }

    @Test
    void toDto_includesFullEmailBodyInPreview() {
        EmailTemplate template = EmailTemplate.builder()
                .id("tpl-1")
                .emailBody("<html>secret</html>")
                .build();

        EmailTemplateDto dto = mapper.toDto(template, true, false);

        assertEquals("<html>secret</html>", dto.getEmailBodyPreview());
    }

    @Test
    void toPreviewDto_includesSmsBodyAndTemplateType_forSmsTemplate() {
        EmailTemplate template = EmailTemplate.builder()
                .id("tpl-sms-1")
                .templateName("SMS Alert")
                .templateType(TemplateType.SMS)
                .smsBody("Hi {{firstName}}, click {{trackingUrl}}")
                .build();

        EmailTemplatePreviewDto dto = mapper.toPreviewDto(template);

        assertEquals(TemplateType.SMS, dto.getTemplateType());
        assertEquals("Hi {{firstName}}, click {{trackingUrl}}", dto.getSmsBody());
    }

    @Test
    void createDuplicate_copiesSmsBodyAndTemplateType() {
        EmailTemplate original = EmailTemplate.builder()
                .templateName("SMS Alert")
                .templateType(TemplateType.SMS)
                .smsBody("Verify your account now")
                .build();

        EmailTemplate duplicate = mapper.createDuplicate(original, "client-1", "user-1", "CLIENT_ADMIN");

        assertEquals(TemplateType.SMS, duplicate.getTemplateType());
        assertEquals("Verify your account now", duplicate.getSmsBody());
        assertEquals("SMS Alert (Copy)", duplicate.getTemplateName());
    }
}
