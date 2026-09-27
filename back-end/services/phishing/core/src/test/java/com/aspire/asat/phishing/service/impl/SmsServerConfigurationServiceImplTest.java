package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.dto.request.SmsServerConfigurationRequest;
import com.aspire.asat.phishing.dto.response.SmsServerConfigurationDto;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.mapper.SmsServerConfigurationMapper;
import com.aspire.asat.phishing.model.SmsServerConfiguration;
import com.aspire.asat.phishing.repository.SmsServerConfigurationRepository;
import com.aspire.asat.phishing.service.ConfigurationAuditService;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import com.aspire.asat.phishing.sms.SmsProviderFactory;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsServerConfigurationServiceImplTest {

    private static final String CLIENT_ID = "client-1";

    @Mock
    private SmsServerConfigurationRepository repository;
    @Mock
    private SmsServerConfigurationMapper mapper;
    @Mock
    private CredentialEncryptionService credentialEncryptionService;
    @Mock
    private ConfigurationAuditService configurationAuditService;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private SmsProviderFactory smsProviderFactory;

    @InjectMocks
    private SmsServerConfigurationServiceImpl service;

    @BeforeEach
    void stubUser() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(CurrentUserContext.builder()
                .clientAdminId(CLIENT_ID)
                .userId("user-1")
                .build());
    }

    @Test
    void create_CustomProviderNameWithBaseUrl_Success() {
        SmsServerConfigurationRequest request = baseCreateRequest("Nexmo");
        request.setBaseUrl("https://rest.nexmo.com");
        SmsServerConfiguration entity = SmsServerConfiguration.builder()
                .clientId(CLIENT_ID)
                .name("Primary SMS")
                .provider("Nexmo")
                .baseUrl("https://rest.nexmo.com")
                .build();
        SmsServerConfiguration saved = SmsServerConfiguration.builder()
                .id("sms-1")
                .clientId(CLIENT_ID)
                .name("Primary SMS")
                .provider("Nexmo")
                .build();

        when(repository.existsByClientIdAndName(CLIENT_ID, "Primary SMS")).thenReturn(false);
        when(mapper.toEntity(request, CLIENT_ID)).thenReturn(entity);
        when(credentialEncryptionService.encrypt(any())).thenAnswer(inv -> "ENC:" + inv.getArgument(0));
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(SmsServerConfigurationDto.builder()
                .id("sms-1")
                .provider("Nexmo")
                .build());

        SmsServerConfigurationDto dto = service.create(request);

        assertEquals("Nexmo", dto.getProvider());
        verify(repository).save(entity);
    }

    @Test
    void create_TwilioProviderWithoutBaseUrl_Success() {
        SmsServerConfigurationRequest request = baseCreateRequest("Twilio");
        SmsServerConfiguration entity = SmsServerConfiguration.builder()
                .clientId(CLIENT_ID)
                .name("Primary SMS")
                .provider("Twilio")
                .build();
        SmsServerConfiguration saved = SmsServerConfiguration.builder()
                .id("sms-2")
                .clientId(CLIENT_ID)
                .name("Primary SMS")
                .provider("Twilio")
                .build();

        when(repository.existsByClientIdAndName(CLIENT_ID, "Primary SMS")).thenReturn(false);
        when(mapper.toEntity(request, CLIENT_ID)).thenReturn(entity);
        when(credentialEncryptionService.encrypt(any())).thenAnswer(inv -> "ENC:" + inv.getArgument(0));
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(SmsServerConfigurationDto.builder()
                .id("sms-2")
                .provider("Twilio")
                .build());

        SmsServerConfigurationDto dto = service.create(request);

        assertEquals("Twilio", dto.getProvider());
        verify(repository).save(entity);
    }

    @Test
    void create_NonTwilioProviderMissingBaseUrl_ThrowsValidationException() {
        SmsServerConfigurationRequest request = baseCreateRequest("Nexmo");

        PhishingValidationException ex = assertThrows(PhishingValidationException.class,
                () -> service.create(request));

        assertEquals("Base URL is required for non-Twilio providers", ex.getMessage());
        verify(repository, never()).save(any());
        verify(mapper, never()).toEntity(any(), any());
    }

    private static SmsServerConfigurationRequest baseCreateRequest(String provider) {
        return SmsServerConfigurationRequest.builder()
                .name("Primary SMS")
                .provider(provider)
                .apiKey("api-key")
                .apiSecret("api-secret")
                .senderId("+15551234567")
                .build();
    }
}
