package com.aspire.asat.cms.service.certificate;

import com.aspire.asat.cms.client.CmsNotificationClient;
import com.aspire.asat.cms.client.service.ClientAdminServiceClient;
import com.aspire.asat.cms.dto.client.responseDto.ExamCertificateResponseDTO;
import com.aspire.asat.cms.repository.CertificateTemplateRepository;
import com.aspire.asat.cms.repository.ClientCertificateTemplateRepository;
import com.aspire.asat.cms.repository.ProductRepository;
import com.aspire.asat.cms.repository.SubPackageRepository;
import com.aspire.asat.cms.repository.UserCertificateRepository;
import com.aspire.asat.cms.repository.UserSubPackageRepository;
import com.aspire.asat.cms.repository.custom.UserCertificateRepositoryCustom;
import com.aspire.asat.cms.util.CertificateGenerator;
import com.aspire.asat.cms.util.UserCurrentContextService;
import com.aspire.asat.common.service.files.FileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateServiceImplExamCertificatesTest {

    @Mock
    private CertificateGenerator certificateGenerator;
    @Mock
    private FileService fileService;
    @Mock
    private UserSubPackageRepository userSubPackageRepository;
    @Mock
    private UserCertificateRepository userCertificateRepository;
    @Mock
    private UserCertificateRepositoryCustom userCertificateRepositoryCustom;
    @Mock
    private SubPackageRepository subPackageRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private CertificateTemplateRepository certificateTemplateRepository;
    @Mock
    private ClientCertificateTemplateRepository clientCertificateTemplateRepository;
    @Mock
    private WebClient webClient;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ClientAdminServiceClient clientAdminServiceClient;
    @Mock
    private CmsNotificationClient cmsNotificationClient;
    @Mock
    private S3Client s3Client;

    @InjectMocks
    private CertificateServiceImpl certificateService;

    @Test
    void getExamCertificates_delegatesToRepositoryWithNormalizedFilters() {
        ExamCertificateResponseDTO dto = ExamCertificateResponseDTO.builder()
                .examId("exam-1")
                .fullName("John Doe")
                .productName("Security Awareness Training")
                .status("VALID")
                .examPassed("Passed")
                .build();
        Page<ExamCertificateResponseDTO> expectedPage =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1);

        when(userCertificateRepositoryCustom.findExamCertificatesWithJoin(
                eq("country-1"),
                eq("msp-1"),
                eq("john"),
                eq("client-admin-1"),
                eq("sub-package-1"),
                eq(PageRequest.of(0, 10))))
                .thenReturn(expectedPage);

        Page<ExamCertificateResponseDTO> result = certificateService.getExamCertificates(
                " country-1 ",
                " msp-1 ",
                " john ",
                " client-admin-1 ",
                " sub-package-1 ",
                0,
                10);

        assertEquals(1, result.getTotalElements());
        assertEquals("exam-1", result.getContent().get(0).getExamId());
        assertEquals("John Doe", result.getContent().get(0).getFullName());
        assertEquals("Security Awareness Training", result.getContent().get(0).getProductName());
        verify(userCertificateRepositoryCustom).findExamCertificatesWithJoin(
                eq("country-1"),
                eq("msp-1"),
                eq("john"),
                eq("client-admin-1"),
                eq("sub-package-1"),
                eq(PageRequest.of(0, 10)));
    }

    @Test
    void getExamCertificates_blankFiltersAreNormalizedToNull() {
        when(userCertificateRepositoryCustom.findExamCertificatesWithJoin(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(PageRequest.of(1, 5))))
                .thenReturn(Page.empty(PageRequest.of(1, 5)));

        certificateService.getExamCertificates(" ", "", "   ", null, "  ", 1, 5);

        verify(userCertificateRepositoryCustom).findExamCertificatesWithJoin(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                eq(PageRequest.of(1, 5)));
    }
}
