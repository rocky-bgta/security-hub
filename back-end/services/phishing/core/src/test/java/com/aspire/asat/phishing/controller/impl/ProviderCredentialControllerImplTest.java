package com.aspire.asat.phishing.controller.impl;

import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.ApiResponseDto;
import com.aspire.asat.phishing.dto.enums.ProviderCategory;
import com.aspire.asat.phishing.dto.request.ProviderCredentialCreateRequest;
import com.aspire.asat.phishing.dto.request.ProviderCredentialUpdateRequest;
import com.aspire.asat.phishing.dto.response.ProviderCredentialDto;
import com.aspire.asat.phishing.service.ProviderCredentialService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderCredentialControllerImplTest {

    @Mock
    private ProviderCredentialService providerCredentialService;

    @InjectMocks
    private ProviderCredentialControllerImpl controller;

    private ProviderCredentialDto dto() {
        return ProviderCredentialDto.builder()
                .id("pc-1").providerName("HEYGEN").category(ProviderCategory.VIDEO_RENDERING)
                .apiKeyLast4("6789").hasApiSecret(false).isActive(true).isDefault(true)
                .build();
    }

    @Test
    void getProviderCredentials_wrapsPageIntoAllResponse() {
        Page<ProviderCredentialDto> page = new PageImpl<>(List.of(dto()), PageRequest.of(0, 10), 1);
        when(providerCredentialService.getProviderCredentials(null, null, 0, 10, "createdAt", "desc"))
                .thenReturn(page);

        ResponseEntity<ApiResponseDto<AllResponseDto<List<ProviderCredentialDto>>>> response =
                controller.getProviderCredentials(null, null, 0, 10, "createdAt", "desc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody().getData().getTotal());
        assertEquals(1, response.getBody().getData().getItems().size());
    }

    @Test
    void create_returns201() {
        ProviderCredentialCreateRequest request = ProviderCredentialCreateRequest.builder()
                .providerName("HEYGEN").category(ProviderCategory.VIDEO_RENDERING).apiKey("k").build();
        when(providerCredentialService.createProviderCredential(request)).thenReturn(dto());

        ResponseEntity<ApiResponseDto<ProviderCredentialDto>> response = controller.createProviderCredential(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("pc-1", response.getBody().getData().getId());
    }

    @Test
    void update_delegatesToService() {
        ProviderCredentialUpdateRequest request = ProviderCredentialUpdateRequest.builder()
                .providerName("HEYGEN").category(ProviderCategory.VIDEO_RENDERING).build();
        when(providerCredentialService.updateProviderCredential("pc-1", request)).thenReturn(dto());

        ResponseEntity<ApiResponseDto<ProviderCredentialDto>> response =
                controller.updateProviderCredential("pc-1", request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void delete_delegatesToService() {
        ResponseEntity<ApiResponseDto<Void>> response = controller.deleteProviderCredential("pc-1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(providerCredentialService).deleteProviderCredential("pc-1");
    }

    @Test
    void setDefault_delegatesToService() {
        when(providerCredentialService.setDefault("pc-1")).thenReturn(dto());
        ResponseEntity<ApiResponseDto<ProviderCredentialDto>> response = controller.setDefault("pc-1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void setActive_delegatesToService() {
        when(providerCredentialService.setActive(eq("pc-1"), any(Boolean.class))).thenReturn(dto());
        ResponseEntity<ApiResponseDto<ProviderCredentialDto>> response = controller.setActive("pc-1", true);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(providerCredentialService).setActive("pc-1", true);
    }

    @Test
    void getById_delegatesToService() {
        when(providerCredentialService.getProviderCredentialById("pc-1")).thenReturn(dto());
        ResponseEntity<ApiResponseDto<ProviderCredentialDto>> response = controller.getProviderCredentialById("pc-1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
