package com.aspire.asat.notification.controller.admin.impl;

import com.aspire.asat.common.enums.notification.NotificationChannel;
import com.aspire.asat.common.enums.notification.NotificationRecipientRole;
import com.aspire.asat.common.enums.notification.NotificationType;
import com.aspire.asat.notification.dto.AllResponseDto;
import com.aspire.asat.notification.dto.ApiResponseDto;
import com.aspire.asat.notification.dto.admin.NotificationTemplateActionDto;
import com.aspire.asat.notification.dto.admin.NotificationTemplateResponseDto;
import com.aspire.asat.notification.dto.admin.NotificationTemplateRoleMatrixDto;
import com.aspire.asat.notification.model.NotificationTemplate;
import com.aspire.asat.notification.service.NotificationTemplateService;
import com.aspire.asat.notification.service.support.NotificationTemplateRoleMatrix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminNotificationTemplateDynamicControllerImplTest {

    @Mock
    private NotificationTemplateService templateService;

    @InjectMocks
    private AdminNotificationTemplateDynamicControllerImpl controller;

    private NotificationTemplate existingEmailTemplate() {
        return NotificationTemplate.builder()
                .id("tpl-1")
                .notificationType(NotificationType.NEW_USER_REGISTERED)
                .channel(NotificationChannel.EMAIL)
                .templateName("New User Registered Email Template")
                .subjectTemplate("New User Registration - {{companyName}}")
                .htmlTemplate("<p>Dear {{adminName}}, user {{userName}} registered.</p>")
                .isActive(true)
                .isDefault(true)
                .build();
    }

    @Test
    void getAllTemplates_returnsAllTemplatesWithTotalCount() {
        when(templateService.getAllTemplates(0, 20, null, null, null, false))
                .thenReturn(List.of(existingEmailTemplate()));
        when(templateService.countAllTemplates(null, null, null, false)).thenReturn(42L);

        ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationTemplateResponseDto>>>> response =
                controller.getAllTemplates(null, null, null, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(42L, response.getBody().getData().getTotal());
        assertEquals(1, response.getBody().getData().getItems().size());
    }

    @Test
    void getAllTemplates_appliesChannelAndNotificationTypeFilters() {
        when(templateService.getAllTemplates(
                0, 20, NotificationType.NEW_USER_REGISTERED, NotificationChannel.EMAIL, null, false))
                .thenReturn(List.of(existingEmailTemplate()));
        when(templateService.countAllTemplates(
                NotificationType.NEW_USER_REGISTERED, NotificationChannel.EMAIL, null, false))
                .thenReturn(1L);

        ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationTemplateResponseDto>>>> response =
                controller.getAllTemplates("EMAIL", "NEW_USER_REGISTERED", null, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(templateService).getAllTemplates(
                0, 20, NotificationType.NEW_USER_REGISTERED, NotificationChannel.EMAIL, null, false);
        verify(templateService).countAllTemplates(
                NotificationType.NEW_USER_REGISTERED, NotificationChannel.EMAIL, null, false);
    }

    @Test
    void getAllTemplates_appliesRecipientRoleFilter() {
        when(templateService.getAllTemplates(0, 20, null, null, NotificationRecipientRole.MSP, true))
                .thenReturn(List.of(existingEmailTemplate()));
        when(templateService.countAllTemplates(null, null, NotificationRecipientRole.MSP, true)).thenReturn(1L);

        ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationTemplateResponseDto>>>> response =
                controller.getAllTemplates(null, null, "msp", 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(templateService).getAllTemplates(0, 20, null, null, NotificationRecipientRole.MSP, true);
    }

    @Test
    void getAllTemplates_baseRoleFilter_selectsRoleAgnosticTemplates() {
        when(templateService.getAllTemplates(0, 20, null, null, null, true))
                .thenReturn(List.of(existingEmailTemplate()));
        when(templateService.countAllTemplates(null, null, null, true)).thenReturn(1L);

        ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationTemplateResponseDto>>>> response =
                controller.getAllTemplates(null, null, "BASE", 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(templateService).getAllTemplates(0, 20, null, null, null, true);
    }

    @Test
    void getAllTemplates_invalidChannelFilter_returnsBadRequest() {
        ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationTemplateResponseDto>>>> response =
                controller.getAllTemplates("INVALID", null, null, 0, 20);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(templateService, never()).getAllTemplates(anyInt(), anyInt(), any(), any(), any(), anyBoolean());
    }

    @Test
    void getAllTemplates_invalidRecipientRoleFilter_returnsBadRequest() {
        ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationTemplateResponseDto>>>> response =
                controller.getAllTemplates(null, null, "SUPERVISOR", 0, 20);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(templateService, never()).getAllTemplates(anyInt(), anyInt(), any(), any(), any(), anyBoolean());
    }

    @Test
    void getTemplateById_returnsTemplateDetails() {
        when(templateService.getTemplateById("tpl-1")).thenReturn(Optional.of(existingEmailTemplate()));

        ResponseEntity<ApiResponseDto<NotificationTemplateResponseDto>> response =
                controller.getTemplateById("tpl-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("tpl-1", response.getBody().getData().getId());
        assertEquals(NotificationType.NEW_USER_REGISTERED, response.getBody().getData().getNotificationType());
    }

    @Test
    void getTemplateById_notFound_returns404() {
        when(templateService.getTemplateById("missing")).thenReturn(Optional.empty());

        ResponseEntity<ApiResponseDto<NotificationTemplateResponseDto>> response =
                controller.getTemplateById("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void update_succeedsAndPersistsContent() {
        when(templateService.getTemplateById("tpl-1")).thenReturn(Optional.of(existingEmailTemplate()));
        when(templateService.updateTemplate(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.UPDATE)
                .templateId("tpl-1")
                .subjectTemplate("Welcome aboard - {{companyName}}")
                .htmlTemplate("<p>Hello {{adminName}}! A new user {{userName}} just signed up.</p>")
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        ArgumentCaptor<NotificationTemplate> captor = ArgumentCaptor.forClass(NotificationTemplate.class);
        verify(templateService).updateTemplate(captor.capture());
        NotificationTemplate saved = captor.getValue();
        assertEquals("Welcome aboard - {{companyName}}", saved.getSubjectTemplate());
        assertEquals("<p>Hello {{adminName}}! A new user {{userName}} just signed up.</p>", saved.getHtmlTemplate());
    }

    @Test
    void update_changingPlaceholders_succeedsAndPersistsContent() {
        when(templateService.getTemplateById("tpl-1")).thenReturn(Optional.of(existingEmailTemplate()));
        when(templateService.updateTemplate(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.UPDATE)
                .templateId("tpl-1")
                .htmlTemplate("<p>Dear {{adminName}}, user {{userName}} registered on {{timestamp}}.</p>")
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        ArgumentCaptor<NotificationTemplate> captor = ArgumentCaptor.forClass(NotificationTemplate.class);
        verify(templateService).updateTemplate(captor.capture());
        assertEquals("<p>Dear {{adminName}}, user {{userName}} registered on {{timestamp}}.</p>",
                captor.getValue().getHtmlTemplate());
    }

    @Test
    void update_doesNotChangeActivationOrIdentityFields() {
        when(templateService.getTemplateById("tpl-1")).thenReturn(Optional.of(existingEmailTemplate()));
        when(templateService.updateTemplate(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.UPDATE)
                .templateId("tpl-1")
                .subjectTemplate("Brand new subject - {{companyName}}")
                .isActive(false)
                .isDefault(false)
                .channel("SMS")
                .build();

        controller.executeAction(action);

        ArgumentCaptor<NotificationTemplate> captor = ArgumentCaptor.forClass(NotificationTemplate.class);
        verify(templateService).updateTemplate(captor.capture());
        NotificationTemplate saved = captor.getValue();
        assertEquals(true, saved.isActive());
        assertEquals(true, saved.isDefault());
        assertEquals(NotificationChannel.EMAIL, saved.getChannel());
        assertEquals(NotificationType.NEW_USER_REGISTERED, saved.getNotificationType());
    }

    @Test
    void update_changingRecipientRole_returnsBadRequest() {
        when(templateService.getTemplateById("tpl-1")).thenReturn(Optional.of(existingEmailTemplate()));

        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.UPDATE)
                .templateId("tpl-1")
                .recipientRole(NotificationRecipientRole.MSP)
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(templateService, never()).updateTemplate(any());
    }

    @Test
    void update_repeatingSameRecipientRole_isAllowed() {
        NotificationTemplate mspVariant = existingEmailTemplate();
        mspVariant.setRecipientRole(NotificationRecipientRole.MSP);
        when(templateService.getTemplateById("tpl-1")).thenReturn(Optional.of(mspVariant));
        when(templateService.updateTemplate(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.UPDATE)
                .templateId("tpl-1")
                .recipientRole(NotificationRecipientRole.MSP)
                .subjectTemplate("MSP subject - {{companyName}}")
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ArgumentCaptor<NotificationTemplate> captor = ArgumentCaptor.forClass(NotificationTemplate.class);
        verify(templateService).updateTemplate(captor.capture());
        assertEquals(NotificationRecipientRole.MSP, captor.getValue().getRecipientRole());
        assertEquals("MSP subject - {{companyName}}", captor.getValue().getSubjectTemplate());
    }

    @Test
    void preview_emailTemplate_returnsRenderedHtml() {
        NotificationTemplate template = existingEmailTemplate();
        when(templateService.getTemplateById("tpl-1")).thenReturn(Optional.of(template));
        when(templateService.generateHtmlContent(template, null)).thenReturn("<p>rendered</p>");

        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.PREVIEW)
                .templateId("tpl-1")
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("<p>rendered</p>", response.getBody().getData());
    }

    @Test
    void preview_withPreviewModel_rendersWithSampleValues() {
        NotificationTemplate template = existingEmailTemplate();
        Map<String, Object> previewModel = Map.of("adminName", "Acme Admin", "userName", "Jane Doe");
        when(templateService.getTemplateById("tpl-1")).thenReturn(Optional.of(template));
        when(templateService.generateHtmlContent(template, previewModel))
                .thenReturn("<p>Dear Acme Admin, user Jane Doe registered.</p>");

        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.PREVIEW)
                .templateId("tpl-1")
                .previewModel(previewModel)
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("<p>Dear Acme Admin, user Jane Doe registered.</p>", response.getBody().getData());
    }

    @Test
    void createRoleVariant_createsVariantFromRequestedTypeChannelAndRole() {
        NotificationTemplate created = existingEmailTemplate();
        created.setId("tpl-msp");
        created.setRecipientRole(NotificationRecipientRole.MSP);
        when(templateService.createRoleVariant(
                eq(NotificationType.NEW_USER_REGISTERED),
                eq(NotificationChannel.EMAIL),
                eq(NotificationRecipientRole.MSP),
                any()))
                .thenReturn(created);

        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.CREATE_ROLE_VARIANT)
                .notificationType("NEW_USER_REGISTERED")
                .channel("EMAIL")
                .recipientRole(NotificationRecipientRole.MSP)
                .subjectTemplate("MSP subject - {{companyName}}")
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("tpl-msp", ((NotificationTemplateResponseDto) response.getBody().getData()).getId());

        ArgumentCaptor<NotificationTemplate> captor = ArgumentCaptor.forClass(NotificationTemplate.class);
        verify(templateService).createRoleVariant(
                eq(NotificationType.NEW_USER_REGISTERED),
                eq(NotificationChannel.EMAIL),
                eq(NotificationRecipientRole.MSP),
                captor.capture());
        assertEquals("MSP subject - {{companyName}}", captor.getValue().getSubjectTemplate());
    }

    @Test
    void createRoleVariant_missingNotificationType_returnsBadRequest() {
        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.CREATE_ROLE_VARIANT)
                .channel("EMAIL")
                .recipientRole(NotificationRecipientRole.MSP)
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(templateService, never()).createRoleVariant(any(), any(), any(), any());
    }

    @Test
    void createRoleVariant_missingChannel_returnsBadRequest() {
        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.CREATE_ROLE_VARIANT)
                .notificationType("NEW_USER_REGISTERED")
                .recipientRole(NotificationRecipientRole.MSP)
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(templateService, never()).createRoleVariant(any(), any(), any(), any());
    }

    @Test
    void createRoleVariant_rejectedByService_returnsBadRequest() {
        when(templateService.createRoleVariant(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("A MSP template already exists"));

        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.CREATE_ROLE_VARIANT)
                .notificationType("NEW_USER_REGISTERED")
                .channel("EMAIL")
                .recipientRole(NotificationRecipientRole.MSP)
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("A MSP template already exists", response.getBody().getMessage());
    }

    @Test
    void delete_roleVariant_deletesTemplate() {
        NotificationTemplate mspVariant = existingEmailTemplate();
        mspVariant.setId("tpl-msp");
        mspVariant.setRecipientRole(NotificationRecipientRole.MSP);
        when(templateService.getTemplateById("tpl-msp")).thenReturn(Optional.of(mspVariant));

        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.DELETE)
                .templateId("tpl-msp")
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(templateService).deleteRoleVariant("tpl-msp");
    }

    @Test
    void delete_unknownTemplate_returns404() {
        when(templateService.getTemplateById("missing")).thenReturn(Optional.empty());

        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.DELETE)
                .templateId("missing")
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(templateService, never()).deleteRoleVariant(any());
    }

    @Test
    void delete_baseTemplateRejectedByService_returnsBadRequest() {
        when(templateService.getTemplateById("tpl-1")).thenReturn(Optional.of(existingEmailTemplate()));
        doThrow(new IllegalArgumentException("Base templates cannot be deleted"))
                .when(templateService).deleteRoleVariant("tpl-1");

        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.DELETE)
                .templateId("tpl-1")
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Base templates cannot be deleted", response.getBody().getMessage());
    }

    @Test
    void getRoleMatrix_marksCustomisedRolesAndInheritedRoles() {
        NotificationTemplate base = existingEmailTemplate();
        NotificationTemplate mspVariant = existingEmailTemplate();
        mspVariant.setId("tpl-msp");
        mspVariant.setTemplateName("New User Registered Email Template (MSP)");
        mspVariant.setRecipientRole(NotificationRecipientRole.MSP);

        when(templateService.getRoleMatrix(NotificationType.NEW_USER_REGISTERED, NotificationChannel.EMAIL))
                .thenReturn(new NotificationTemplateRoleMatrix(base, Map.of(NotificationRecipientRole.MSP, mspVariant)));

        ResponseEntity<ApiResponseDto<NotificationTemplateRoleMatrixDto>> response =
                controller.getRoleMatrix("NEW_USER_REGISTERED", "EMAIL");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        NotificationTemplateRoleMatrixDto matrix = response.getBody().getData();
        assertEquals("tpl-1", matrix.getBaseTemplate().getId());
        assertEquals(NotificationRecipientRole.values().length, matrix.getRoles().size());

        NotificationTemplateRoleMatrixDto.RoleTemplateEntryDto mspEntry = matrix.getRoles().stream()
                .filter(entry -> entry.getRole() == NotificationRecipientRole.MSP)
                .findFirst()
                .orElseThrow();
        assertEquals(NotificationTemplateRoleMatrixDto.RoleTemplateStatus.CUSTOM, mspEntry.getStatus());
        assertEquals("tpl-msp", mspEntry.getTemplateId());
        assertEquals("New User Registered Email Template (MSP)", mspEntry.getTemplateName());

        NotificationTemplateRoleMatrixDto.RoleTemplateEntryDto userEntry = matrix.getRoles().stream()
                .filter(entry -> entry.getRole() == NotificationRecipientRole.USER)
                .findFirst()
                .orElseThrow();
        assertEquals(NotificationTemplateRoleMatrixDto.RoleTemplateStatus.INHERITS_BASE, userEntry.getStatus());
        assertNull(userEntry.getTemplateId());
    }

    @Test
    void getRoleMatrix_invalidNotificationType_returnsBadRequest() {
        ResponseEntity<ApiResponseDto<NotificationTemplateRoleMatrixDto>> response =
                controller.getRoleMatrix("NOT_A_TYPE", "EMAIL");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(templateService, never()).getRoleMatrix(any(), any());
    }

    @Test
    void update_missingTemplateId_returnsBadRequest() {
        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.UPDATE)
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(templateService, never()).updateTemplate(any());
    }

    @Test
    void getAllTemplates_hiddenNotificationType_returnsBadRequest() {
        ResponseEntity<ApiResponseDto<AllResponseDto<List<NotificationTemplateResponseDto>>>> response =
                controller.getAllTemplates(null, "PACKAGE_ASSIGNED", null, 0, 20);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(templateService, never()).getAllTemplates(anyInt(), anyInt(), any(), any(), any(), anyBoolean());
    }

    @Test
    void getRoleMatrix_hiddenNotificationType_returnsBadRequest() {
        ResponseEntity<ApiResponseDto<NotificationTemplateRoleMatrixDto>> response =
                controller.getRoleMatrix("COURSE_COMPLETION", "EMAIL");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(templateService, never()).getRoleMatrix(any(), any());
    }

    @Test
    void update_hiddenNotificationType_returnsBadRequest() {
        NotificationTemplate hiddenTemplate = NotificationTemplate.builder()
                .id("tpl-hidden")
                .notificationType(NotificationType.PACKAGE_ASSIGNED)
                .channel(NotificationChannel.EMAIL)
                .templateName("Package Assigned")
                .isActive(true)
                .isDefault(true)
                .build();
        when(templateService.getTemplateById("tpl-hidden")).thenReturn(Optional.of(hiddenTemplate));

        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.UPDATE)
                .templateId("tpl-hidden")
                .subjectTemplate("Updated")
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(templateService, never()).updateTemplate(any());
    }

    @Test
    void createRoleVariant_hiddenNotificationType_returnsBadRequest() {
        NotificationTemplateActionDto action = NotificationTemplateActionDto.builder()
                .action(NotificationTemplateActionDto.Action.CREATE_ROLE_VARIANT)
                .notificationType("USER_PROFILE_UPDATED")
                .channel("EMAIL")
                .recipientRole(NotificationRecipientRole.MSP)
                .build();

        ResponseEntity<ApiResponseDto<Object>> response = controller.executeAction(action);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(templateService, never()).createRoleVariant(any(), any(), any(), any());
    }
}
