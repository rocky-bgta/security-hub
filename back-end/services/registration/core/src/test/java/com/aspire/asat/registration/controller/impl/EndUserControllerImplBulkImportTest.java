package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportOnboardResponseDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportRowStatus;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportUpdateRequestDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportUserDto;
import com.aspire.asat.registration.data.endUser.bulkimport.BulkImportValidateResponseDto;
import com.aspire.asat.registration.service.BulkUserImportService;
import com.aspire.asat.registration.service.EndUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndUserControllerImplBulkImportTest {

    private static final String SESSION_ID = "session-abc";
    private static final String CLIENT_ADMIN_ID = "client-admin-1";

    @Mock
    private EndUserService endUserService;
    @Mock
    private BulkUserImportService bulkUserImportService;
    @Mock
    private MessageService messageService;

    @InjectMocks
    private EndUserControllerImpl controller;

    @Test
    void validateBulkImport_ReturnsOkEnvelope() {
        MultipartFile file = mock(MultipartFile.class);
        BulkImportValidateResponseDto data = BulkImportValidateResponseDto.builder()
                .importSessionId(SESSION_ID)
                .totalValid(2)
                .totalInvalid(1)
                .build();
        when(bulkUserImportService.validateImport(file, CLIENT_ADMIN_ID, 0, 10)).thenReturn(data);

        ResponseEntity<ApiResponseDto<BulkImportValidateResponseDto>> response =
                controller.validateBulkImport(file, CLIENT_ADMIN_ID, 0, 10);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("File uploaded successfully and validated.", response.getBody().getMessage());
        assertEquals(SESSION_ID, response.getBody().getData().getImportSessionId());
        verify(bulkUserImportService).validateImport(file, CLIENT_ADMIN_ID, 0, 10);
    }

    @Test
    void getBulkImportUsers_ReturnsOkEnvelope() {
        AllResponseDto<List<BulkImportUserDto>> page = new AllResponseDto<>(0, 10, 1L,
                List.of(BulkImportUserDto.builder().rowIndex(1).email("a@company.com").build()));
        when(bulkUserImportService.getSessionUsers(SESSION_ID, BulkImportRowStatus.VALID, 0, 10))
                .thenReturn(page);

        ResponseEntity<ApiResponseDto<AllResponseDto<List<BulkImportUserDto>>>> response =
                controller.getBulkImportUsers(SESSION_ID, BulkImportRowStatus.VALID, 0, 10);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Bulk import users retrieved successfully", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().getItems().size());
        verify(bulkUserImportService).getSessionUsers(SESSION_ID, BulkImportRowStatus.VALID, 0, 10);
    }

    @Test
    void updateBulkImportUsers_ReturnsOkEnvelope() {
        BulkImportUpdateRequestDto request = BulkImportUpdateRequestDto.builder()
                .users(List.of(BulkImportUserDto.builder().rowIndex(1).email("fixed@company.com").build()))
                .build();
        BulkImportValidateResponseDto data = BulkImportValidateResponseDto.builder()
                .importSessionId(SESSION_ID)
                .totalValid(1)
                .totalInvalid(0)
                .build();
        when(bulkUserImportService.updateSessionUsers(SESSION_ID, request, 0, 10)).thenReturn(data);

        ResponseEntity<ApiResponseDto<BulkImportValidateResponseDto>> response =
                controller.updateBulkImportUsers(SESSION_ID, request, 0, 10);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Bulk import users updated successfully", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().getTotalValid());
        verify(bulkUserImportService).updateSessionUsers(SESSION_ID, request, 0, 10);
    }

    @Test
    void onboardBulkImport_ReturnsOkEnvelope() {
        BulkImportOnboardResponseDto data = BulkImportOnboardResponseDto.builder()
                .totalUsers(2)
                .successful(1)
                .failed(1)
                .failedUsers(List.of())
                .build();
        when(bulkUserImportService.onboardSession(SESSION_ID)).thenReturn(data);

        ResponseEntity<ApiResponseDto<BulkImportOnboardResponseDto>> response =
                controller.onboardBulkImport(SESSION_ID);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Bulk import completed", response.getBody().getMessage());
        assertEquals(1, response.getBody().getData().getSuccessful());
        verify(bulkUserImportService).onboardSession(SESSION_ID);
    }
}
