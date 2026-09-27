package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.clientAdmin.response.LicenseOverviewSummaryDto;
import com.aspire.asat.registration.exception.RegistrationServiceException;
import com.aspire.asat.registration.service.ClientAdminService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientAdminControllerImplLicenseOverviewSummaryTest {

    private static final String CLIENT_ADMIN_ID = "client-admin-1";

    @Mock
    private ClientAdminService clientAdminService;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private MessageService messageService;

    @InjectMocks
    private ClientAdminControllerImpl controller;

    @Test
    void getLicenseOverviewSummary_queryParam_usesProvidedClientAdminId() {
        LicenseOverviewSummaryDto summary = LicenseOverviewSummaryDto.builder().totalLicenses(995).build();
        when(clientAdminService.getLicenseOverviewSummary(CLIENT_ADMIN_ID)).thenReturn(summary);

        ResponseEntity<ApiResponseDto<LicenseOverviewSummaryDto>> response =
                controller.getLicenseOverviewSummary("  " + CLIENT_ADMIN_ID + " ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals(995, response.getBody().getData().getTotalLicenses());
        verify(userCurrentContextService, never()).getCurrentUserContext();
    }

    @Test
    void getLicenseOverviewSummary_blankParam_usesContextClientAdminId() {
        CurrentUserContext context = new CurrentUserContext();
        context.setClientAdminId(CLIENT_ADMIN_ID);
        context.setUserId("user-id");
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(clientAdminService.getLicenseOverviewSummary(CLIENT_ADMIN_ID))
                .thenReturn(LicenseOverviewSummaryDto.builder().assignedUsers(850).build());

        ResponseEntity<ApiResponseDto<LicenseOverviewSummaryDto>> response =
                controller.getLicenseOverviewSummary("  ");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(850, response.getBody().getData().getAssignedUsers());
        verify(clientAdminService).getLicenseOverviewSummary(CLIENT_ADMIN_ID);
    }

    @Test
    void getLicenseOverviewSummary_noClientAdminIdInContext_fallsBackToUserId() {
        CurrentUserContext context = new CurrentUserContext();
        context.setUserId(CLIENT_ADMIN_ID);
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(clientAdminService.getLicenseOverviewSummary(CLIENT_ADMIN_ID))
                .thenReturn(LicenseOverviewSummaryDto.builder().activeProducts(6).build());

        ResponseEntity<ApiResponseDto<LicenseOverviewSummaryDto>> response =
                controller.getLicenseOverviewSummary(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(6, response.getBody().getData().getActiveProducts());
    }

    @Test
    void getLicenseOverviewSummary_unresolvedClientAdminId_returns400() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(new CurrentUserContext());

        ResponseEntity<ApiResponseDto<LicenseOverviewSummaryDto>> response =
                controller.getLicenseOverviewSummary(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatusCode());
        assertNull(response.getBody().getData());
        verify(clientAdminService, never()).getLicenseOverviewSummary(anyString());
    }

    @Test
    void getLicenseOverviewSummary_clientAdminMissing_returns404() {
        when(clientAdminService.getLicenseOverviewSummary(CLIENT_ADMIN_ID))
                .thenThrow(new RegistrationServiceException("Client admin not found with ID: " + CLIENT_ADMIN_ID));

        ResponseEntity<ApiResponseDto<LicenseOverviewSummaryDto>> response =
                controller.getLicenseOverviewSummary(CLIENT_ADMIN_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getStatusCode());
        assertNull(response.getBody().getData());
    }

    @Test
    void getLicenseOverviewSummary_unexpectedError_returns500() {
        when(clientAdminService.getLicenseOverviewSummary(CLIENT_ADMIN_ID))
                .thenThrow(new IllegalStateException("boom"));

        ResponseEntity<ApiResponseDto<LicenseOverviewSummaryDto>> response =
                controller.getLicenseOverviewSummary(CLIENT_ADMIN_ID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(500, response.getBody().getStatusCode());
        assertNull(response.getBody().getData());
    }
}
