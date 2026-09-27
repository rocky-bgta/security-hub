package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.PhishingNotificationClient;
import com.aspire.asat.phishing.dto.enums.DomainStatus;
import com.aspire.asat.phishing.dto.request.DomainVerificationRequest;
import com.aspire.asat.phishing.dto.request.GenerateVerificationRequest;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.mapper.DomainMapper;
import com.aspire.asat.phishing.model.Domain;
import com.aspire.asat.phishing.model.DomainVerificationCode;
import com.aspire.asat.phishing.repository.DomainRepository;
import com.aspire.asat.phishing.repository.DomainVerificationCodeRepository;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainServiceImplDuplicateVerifyTest {

    private static final String CLIENT_ID = "client-1";
    private static final String USER_ID = "user-1";
    private static final String DOMAIN_NAME = "example.com";
    private static final String EMAIL = "admin@example.com";
    private static final String CODE = "123456";

    @Mock private DomainRepository domainRepository;
    @Mock private DomainVerificationCodeRepository verificationCodeRepository;
    @Mock private DomainMapper domainMapper;
    @Mock private UserCurrentContextService userCurrentContextService;
    @Mock private PhishingNotificationClient phishingNotificationClient;

    @InjectMocks
    private DomainServiceImpl domainService;

    @BeforeEach
    void setUp() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder()
                        .clientAdminId(CLIENT_ID)
                        .userId(USER_ID)
                        .build());
    }

    @Test
    void verifyDomain_alreadyVerified_throwsConflictAndDoesNotMutate() {
        DomainVerificationCode verificationCode = DomainVerificationCode.builder()
                .id("code-1")
                .clientId(CLIENT_ID)
                .domain(DOMAIN_NAME)
                .emailAddress(EMAIL)
                .verificationCode(CODE)
                .expiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
                .isUsed(false)
                .build();
        when(verificationCodeRepository.findByDomainAndEmailAddressAndIsUsedFalseAndExpiresAtAfter(
                any(), any(), any())).thenReturn(Optional.of(verificationCode));
        when(domainRepository.findByDomain(DOMAIN_NAME)).thenReturn(Optional.of(Domain.builder()
                .id("domain-1")
                .domain(DOMAIN_NAME)
                .clientId(CLIENT_ID)
                .status(DomainStatus.VERIFIED)
                .build()));

        DomainVerificationRequest request = DomainVerificationRequest.builder()
                .emailAddress(EMAIL)
                .verificationCode(CODE)
                .build();

        ServiceException ex = assertThrows(ServiceException.class, () -> domainService.verifyDomain(request));

        assertEquals("This domain is already verified", ex.getMessage());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(verificationCodeRepository, never()).save(any());
        verify(domainRepository, never()).save(any());
    }

    @Test
    void generateVerificationEmail_alreadyVerified_throwsConflictAndDoesNotSaveCode() {
        when(verificationCodeRepository.countByDomainAndCreatedAtAfter(any(), any())).thenReturn(0L);
        when(domainRepository.existsByDomainAndIsLockedTrueAndClientIdNot(DOMAIN_NAME, CLIENT_ID)).thenReturn(false);
        when(domainRepository.findByDomain(DOMAIN_NAME)).thenReturn(Optional.of(Domain.builder()
                .id("domain-1")
                .domain(DOMAIN_NAME)
                .clientId(CLIENT_ID)
                .status(DomainStatus.VERIFIED)
                .build()));

        GenerateVerificationRequest request = GenerateVerificationRequest.builder()
                .emailAddress(EMAIL)
                .build();

        ServiceException ex = assertThrows(ServiceException.class,
                () -> domainService.generateVerificationEmail(request));

        assertEquals("This domain is already verified", ex.getMessage());
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(verificationCodeRepository, never()).save(any());
        verify(phishingNotificationClient, never()).sendDomainVerificationEmail(any(), any(), any());
    }
}
