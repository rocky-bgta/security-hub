package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.response.SenderProfileImportResultDto;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.SenderProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SenderProfileControllerImplTest {

    @Mock
    private SenderProfileService senderProfileService;

    @InjectMocks
    private SenderProfileControllerImpl senderProfileController;

    @Test
    void importSenderProfilesShouldReturnOkForSuccessfulImport() {
        MultipartFile file = mock(MultipartFile.class);

        SenderProfileImportResultDto result = SenderProfileImportResultDto.builder()
                .totalRows(2)
                .successCount(1)
                .failedCount(1)
                .importedProfiles(List.of())
                .errors(List.of())
                .build();

        when(senderProfileService.importSenderProfiles(file)).thenReturn(result);

        ResponseEntity<ApiResponseDto<SenderProfileImportResultDto>> response =
                senderProfileController.importSenderProfiles(file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals("Sender profiles imported successfully", response.getBody().getMessage());
        assertEquals(2, response.getBody().getData().getTotalRows());
    }

    @Test
    void importSenderProfilesShouldReturnBadRequestOnServiceException() {
        MultipartFile file = mock(MultipartFile.class);
        when(senderProfileService.importSenderProfiles(file)).thenThrow(new ServiceException("Invalid CSV"));

        ResponseEntity<ApiResponseDto<SenderProfileImportResultDto>> response =
                senderProfileController.importSenderProfiles(file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().getStatusCode());
        assertEquals("Invalid CSV", response.getBody().getMessage());
    }
}
