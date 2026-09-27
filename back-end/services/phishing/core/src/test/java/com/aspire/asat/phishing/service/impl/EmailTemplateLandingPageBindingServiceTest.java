package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.mapper.EmailTemplateMapper;
import com.aspire.asat.phishing.mapper.LandingPageMapper;
import com.aspire.asat.phishing.model.EmailTemplate;
import com.aspire.asat.phishing.model.LandingPage;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailTemplateLandingPageBindingServiceTest {

    private static final String CLIENT_ID = "client-1";
    private static final String TEMPLATE_ID = "template-1";
    private static final String SMS_TEMPLATE_ID = "sms-template-1";
    private static final String PAGE_ID = "page-1";
    private static final String PAGE_ID_2 = "page-2";

    @Mock
    private EmailTemplateRepository emailTemplateRepository;

    @Mock
    private LandingPageRepository landingPageRepository;

    @Mock
    private EmailTemplateMapper emailTemplateMapper;

    @Mock
    private LandingPageMapper landingPageMapper;

    @InjectMocks
    private EmailTemplateLandingPageBindingServiceImpl bindingService;

    @Test
    void setLandingPagesForTemplate_ReplacesBindings_Success() {
        EmailTemplate template = emailTemplate(TEMPLATE_ID, TemplateType.EMAIL);
        when(emailTemplateRepository.findByIdAndClientIdOrGlobal(TEMPLATE_ID, CLIENT_ID))
                .thenReturn(Optional.of(template));
        when(landingPageRepository.findByIdAndClientIdOrGlobal(PAGE_ID, CLIENT_ID))
                .thenReturn(Optional.of(landingPage(PAGE_ID)));
        when(landingPageRepository.findByIdAndClientIdOrGlobal(PAGE_ID_2, CLIENT_ID))
                .thenReturn(Optional.of(landingPage(PAGE_ID_2)));

        bindingService.setLandingPagesForTemplate(TEMPLATE_ID, List.of(PAGE_ID, PAGE_ID_2), CLIENT_ID);

        ArgumentCaptor<EmailTemplate> captor = ArgumentCaptor.forClass(EmailTemplate.class);
        verify(emailTemplateRepository).save(captor.capture());
        assertEquals(List.of(PAGE_ID, PAGE_ID_2), captor.getValue().getLandingPageIds());
    }

    @Test
    void setLandingPagesForTemplate_TemplateNotFound_Throws() {
        when(emailTemplateRepository.findByIdAndClientIdOrGlobal(TEMPLATE_ID, CLIENT_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bindingService.setLandingPagesForTemplate(TEMPLATE_ID, List.of(PAGE_ID), CLIENT_ID));
    }

    @Test
    void addLandingPageToTemplates_AppendsToSmsTemplate_Success() {
        EmailTemplate smsTemplate = emailTemplate(SMS_TEMPLATE_ID, TemplateType.SMS);
        when(landingPageRepository.findByIdAndClientIdOrGlobal(PAGE_ID, CLIENT_ID))
                .thenReturn(Optional.of(landingPage(PAGE_ID)));
        when(emailTemplateRepository.findByIdAndClientIdOrGlobal(SMS_TEMPLATE_ID, CLIENT_ID))
                .thenReturn(Optional.of(smsTemplate));

        bindingService.addLandingPageToTemplates(PAGE_ID, List.of(SMS_TEMPLATE_ID), CLIENT_ID);

        ArgumentCaptor<EmailTemplate> captor = ArgumentCaptor.forClass(EmailTemplate.class);
        verify(emailTemplateRepository).save(captor.capture());
        assertEquals(List.of(PAGE_ID), captor.getValue().getLandingPageIds());
    }

    @Test
    void addLandingPageToTemplates_DeduplicatesExistingBinding() {
        EmailTemplate template = emailTemplate(TEMPLATE_ID, TemplateType.EMAIL);
        template.setLandingPageIds(new ArrayList<>(List.of(PAGE_ID)));
        when(landingPageRepository.findByIdAndClientIdOrGlobal(PAGE_ID, CLIENT_ID))
                .thenReturn(Optional.of(landingPage(PAGE_ID)));
        when(emailTemplateRepository.findByIdAndClientIdOrGlobal(TEMPLATE_ID, CLIENT_ID))
                .thenReturn(Optional.of(template));

        bindingService.addLandingPageToTemplates(PAGE_ID, List.of(TEMPLATE_ID), CLIENT_ID);

        verify(emailTemplateRepository, never()).save(any());
    }

    @Test
    void syncTemplatesForLandingPage_RemovesOldAndAddsNew() {
        EmailTemplate keepTemplate = emailTemplate("keep-template", TemplateType.EMAIL);
        keepTemplate.setLandingPageIds(new ArrayList<>(List.of(PAGE_ID)));
        EmailTemplate removeTemplate = emailTemplate("remove-template", TemplateType.EMAIL);
        removeTemplate.setLandingPageIds(new ArrayList<>(List.of(PAGE_ID)));
        EmailTemplate addTemplate = emailTemplate("add-template", TemplateType.SMS);

        when(landingPageRepository.findByIdAndClientIdOrGlobal(PAGE_ID, CLIENT_ID))
                .thenReturn(Optional.of(landingPage(PAGE_ID)));
        when(emailTemplateRepository.findByLandingPageIdsContaining(PAGE_ID))
                .thenReturn(List.of(keepTemplate, removeTemplate));
        when(emailTemplateRepository.findByIdAndClientIdOrGlobal("keep-template", CLIENT_ID))
                .thenReturn(Optional.of(keepTemplate));
        when(emailTemplateRepository.findByIdAndClientIdOrGlobal("add-template", CLIENT_ID))
                .thenReturn(Optional.of(addTemplate));

        bindingService.syncTemplatesForLandingPage(PAGE_ID, List.of("keep-template", "add-template"), CLIENT_ID);

        verify(emailTemplateRepository, times(2)).save(any(EmailTemplate.class));
        assertTrue(removeTemplate.getLandingPageIds().isEmpty());
        assertEquals(List.of(PAGE_ID), addTemplate.getLandingPageIds());
    }

    @Test
    void removeLandingPageFromAllTemplates_ClearsBindings() {
        EmailTemplate template = emailTemplate(TEMPLATE_ID, TemplateType.EMAIL);
        template.setLandingPageIds(new ArrayList<>(List.of(PAGE_ID, PAGE_ID_2)));
        when(emailTemplateRepository.findByLandingPageIdsContaining(PAGE_ID))
                .thenReturn(List.of(template));

        bindingService.removeLandingPageFromAllTemplates(PAGE_ID);

        ArgumentCaptor<EmailTemplate> captor = ArgumentCaptor.forClass(EmailTemplate.class);
        verify(emailTemplateRepository).save(captor.capture());
        assertEquals(List.of(PAGE_ID_2), captor.getValue().getLandingPageIds());
    }

    @Test
    void getBoundLandingPages_PreservesOrderAndFiltersMissing() {
        EmailTemplate template = emailTemplate(TEMPLATE_ID, TemplateType.EMAIL);
        template.setLandingPageIds(List.of(PAGE_ID, "missing-page", PAGE_ID_2));
        LandingPage page1 = landingPage(PAGE_ID);
        LandingPage page2 = landingPage(PAGE_ID_2);

        when(emailTemplateRepository.findByIdAndClientIdOrGlobal(TEMPLATE_ID, CLIENT_ID))
                .thenReturn(Optional.of(template));
        when(landingPageRepository.findAllById(List.of(PAGE_ID, "missing-page", PAGE_ID_2)))
                .thenReturn(List.of(page1, page2));
        when(landingPageMapper.toDto(eq(page1), eq(false), eq(false))).thenReturn(
                com.aspire.asat.phishing.dto.response.LandingPageDto.builder().pageId(PAGE_ID).build());
        when(landingPageMapper.toDto(eq(page2), eq(false), eq(false))).thenReturn(
                com.aspire.asat.phishing.dto.response.LandingPageDto.builder().pageId(PAGE_ID_2).build());

        var result = bindingService.getBoundLandingPages(TEMPLATE_ID, CLIENT_ID);

        assertEquals(2, result.size());
        assertEquals(PAGE_ID, result.get(0).getPageId());
        assertEquals(PAGE_ID_2, result.get(1).getPageId());
    }

    @Test
    void getBoundEmailTemplateIds_ReturnsAccessibleTemplateIds() {
        EmailTemplate emailTemplate = emailTemplate(TEMPLATE_ID, TemplateType.EMAIL);
        EmailTemplate smsTemplate = emailTemplate(SMS_TEMPLATE_ID, TemplateType.SMS);
        when(emailTemplateRepository.findByLandingPageIdsContainingAndClientIdOrGlobal(PAGE_ID, CLIENT_ID))
                .thenReturn(List.of(emailTemplate, smsTemplate));

        List<String> result = bindingService.getBoundEmailTemplateIds(PAGE_ID, CLIENT_ID);

        assertEquals(List.of(TEMPLATE_ID, SMS_TEMPLATE_ID), result);
    }

    private EmailTemplate emailTemplate(String id, TemplateType type) {
        return EmailTemplate.builder()
                .id(id)
                .clientId(CLIENT_ID)
                .templateName("Template " + id)
                .templateType(type)
                .landingPageIds(new ArrayList<>())
                .build();
    }

    private LandingPage landingPage(String id) {
        return LandingPage.builder()
                .id(id)
                .clientId(CLIENT_ID)
                .name("Page " + id)
                .build();
    }
}
