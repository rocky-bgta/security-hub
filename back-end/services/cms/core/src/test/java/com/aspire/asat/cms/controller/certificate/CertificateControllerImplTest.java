package com.aspire.asat.cms.controller.certificate;

import com.aspire.asat.cms.dto.client.responseDto.CertificateDetailsResponseDTO;
import com.aspire.asat.cms.dto.enums.CertificateStatus;
import com.aspire.asat.cms.service.certificate.CertificateService;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateControllerImplTest {

    @Mock
    private CertificateService certificateService;

    @Mock
    private UserCurrentContextService userCurrentContextService;

    @InjectMocks
    private CertificateControllerImpl controller;

    @Test
    void getCertificateDetails_withoutOptionalFilters_forwardsNullProductAndStatus() {
        CurrentUserContext context = CurrentUserContext.builder().userId("admin-1").build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(certificateService.getCertificateDetails(
                any(), any(), any(), any(), any(), any(), any(), eq(0), eq(10)))
                .thenReturn(List.of());
        when(certificateService.countCertificateDetails(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0L);

        ResponseEntity<?> response = controller.getCertificateDetails(
                true, null, null, null, null, null, 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(certificateService).getCertificateDetails(
                eq(context), eq(true), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10));
        verify(certificateService).countCertificateDetails(
                eq(context), eq(true), isNull(), isNull(), isNull(), isNull(), isNull());
    }

    @Test
    void getCertificateDetails_forwardsProductAndStatusFilters() {
        CurrentUserContext context = CurrentUserContext.builder().userId("admin-1").build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(certificateService.getCertificateDetails(
                any(), any(), any(), any(), any(), any(), any(), eq(0), eq(10)))
                .thenReturn(List.of());
        when(certificateService.countCertificateDetails(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(0L);

        controller.getCertificateDetails(
                true, null, null, "cert", "product-1", CertificateStatus.VALID, 0, 10);

        verify(certificateService).getCertificateDetails(
                eq(context), eq(true), isNull(), isNull(), eq("cert"), eq("product-1"),
                eq(CertificateStatus.VALID), eq(0), eq(10));
        verify(certificateService).countCertificateDetails(
                eq(context), eq(true), isNull(), isNull(), eq("cert"), eq("product-1"),
                eq(CertificateStatus.VALID));
    }
}
