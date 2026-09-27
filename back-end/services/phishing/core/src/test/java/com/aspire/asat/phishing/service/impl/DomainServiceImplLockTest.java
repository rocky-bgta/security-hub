package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.client.PhishingNotificationClient;
import com.aspire.asat.phishing.dto.enums.DomainStatus;
import com.aspire.asat.phishing.exception.ResourceNotFoundException;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.mapper.DomainMapper;
import com.aspire.asat.phishing.model.Domain;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainServiceImplLockTest {

    private static final String CLIENT_ID = "client-1";
    private static final String USER_ID = "user-1";
    private static final String DOMAIN_ID = "domain-1";

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
    void lockDomain_notVerified_throwsServiceExceptionWithExactMessage() {
        Domain domain = Domain.builder()
                .id(DOMAIN_ID)
                .clientId(CLIENT_ID)
                .domain("example.com")
                .status(DomainStatus.UNVERIFIED)
                .build();
        when(domainRepository.findById(DOMAIN_ID)).thenReturn(Optional.of(domain));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> domainService.lockDomain(DOMAIN_ID));

        assertEquals("Domain must be verified before locking", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void lockDomain_missing_throwsResourceNotFound() {
        when(domainRepository.findById(DOMAIN_ID)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> domainService.lockDomain(DOMAIN_ID));

        assertEquals("Domain not found", ex.getMessage());
    }
}
