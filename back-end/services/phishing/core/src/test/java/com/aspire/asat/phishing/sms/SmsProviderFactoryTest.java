package com.aspire.asat.phishing.sms;

import com.aspire.asat.phishing.dto.enums.SmsProviderType;
import com.aspire.asat.phishing.model.SmsServerConfiguration;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import com.aspire.asat.phishing.sms.impl.AnbernetSmsProvider;
import com.aspire.asat.phishing.sms.impl.GenericRestSmsProvider;
import com.aspire.asat.phishing.sms.impl.TwilioSmsProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmsProviderFactoryTest {

    @Mock
    private CredentialEncryptionService credentialEncryptionService;

    @InjectMocks
    private SmsProviderFactory factory;

    @Test
    void create_TwilioIgnoreCase_ReturnsTwilioProvider() {
        stubDecrypt();
        SmsServerConfiguration config = SmsServerConfiguration.builder()
                .provider("twilio")
                .apiKey("enc-key")
                .apiSecret("enc-secret")
                .senderId("+15551234567")
                .build();

        SmsProvider provider = factory.create(config);

        assertInstanceOf(TwilioSmsProvider.class, provider);
    }

    @Test
    void create_CustomProviderName_ReturnsGenericRestProvider() {
        stubDecrypt();
        SmsServerConfiguration config = SmsServerConfiguration.builder()
                .provider("Nexmo")
                .apiKey("enc-key")
                .apiSecret("enc-secret")
                .senderId("+15551234567")
                .baseUrl("https://rest.nexmo.com")
                .build();

        SmsProvider provider = factory.create(config);

        assertInstanceOf(GenericRestSmsProvider.class, provider);
    }

    @Test
    void create_BdProvider_ReturnsAnbernetProvider() {
        stubDecrypt();
        SmsServerConfiguration config = SmsServerConfiguration.builder()
                .name("Aspire-Tech")
                .provider("BD")
                .apiKey("enc-key")
                .apiSecret("enc-secret")
                .senderId("8809639210610")
                .baseUrl("https://wapi.anbernet.com:9956/api/v1/sendsms")
                .build();

        SmsProvider provider = factory.create(config);

        assertInstanceOf(AnbernetSmsProvider.class, provider);
        assertEquals(SmsProviderType.GENERIC_REST, provider.getProviderType());
    }

    @Test
    void create_AnbernetBaseUrlWithCustomProviderName_ReturnsAnbernetProvider() {
        stubDecrypt();
        SmsServerConfiguration config = SmsServerConfiguration.builder()
                .name("Aspire-Tech")
                .provider("Aspire-Tech")
                .apiKey("enc-key")
                .apiSecret("enc-secret")
                .senderId("8809639210610")
                .baseUrl("https://wapi.anbernet.com:9956/api/v1/sendsms")
                .build();

        SmsProvider provider = factory.create(config);

        assertInstanceOf(AnbernetSmsProvider.class, provider);
    }

    @Test
    void isTwilio_MatchesIgnoreCaseAndRejectsOtherNames() {
        assertTrue(SmsProviderFactory.isTwilio("TWILIO"));
        assertTrue(SmsProviderFactory.isTwilio("Twilio"));
        assertTrue(SmsProviderFactory.isTwilio(" twilio "));
        assertFalse(SmsProviderFactory.isTwilio("Nexmo"));
        assertFalse(SmsProviderFactory.isTwilio("GENERIC_REST"));
        assertFalse(SmsProviderFactory.isTwilio(null));
    }

    @Test
    void isAnbernet_MatchesProviderNameOrAnbernetUrl() {
        assertTrue(SmsProviderFactory.isAnbernet("BD", null));
        assertTrue(SmsProviderFactory.isAnbernet("anbernet", "https://example.com"));
        assertTrue(SmsProviderFactory.isAnbernet("Aspire-Tech",
                "https://wapi.anbernet.com:9956/api/v1/sendsms"));
        assertFalse(SmsProviderFactory.isAnbernet("Nexmo", "https://rest.nexmo.com"));
        assertFalse(SmsProviderFactory.isAnbernet(null, null));
    }

    private void stubDecrypt() {
        when(credentialEncryptionService.decrypt(anyString())).thenAnswer(inv -> inv.getArgument(0));
    }
}
