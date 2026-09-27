package com.aspire.asat.phishing.sms;

import com.aspire.asat.phishing.model.SmsServerConfiguration;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import com.aspire.asat.phishing.sms.impl.AnbernetSmsProvider;
import com.aspire.asat.phishing.sms.impl.GenericRestSmsProvider;
import com.aspire.asat.phishing.sms.impl.TwilioSmsProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class SmsProviderFactory {

    private static final String TWILIO = "TWILIO";
    private static final Set<String> ANBERNET_PROVIDERS = Set.of("BD", "ANBERNET", "BANGLADESH");

    private final CredentialEncryptionService credentialEncryptionService;

    public SmsProvider create(SmsServerConfiguration config) {
        String apiKey = credentialEncryptionService.decrypt(config.getApiKey());
        String apiSecret = credentialEncryptionService.decrypt(config.getApiSecret());
        if (isTwilio(config.getProvider())) {
            return new TwilioSmsProvider(apiKey, apiSecret, config.getSenderId());
        }
        if (isAnbernet(config.getProvider(), config.getBaseUrl())) {
            return new AnbernetSmsProvider(config.getName(), apiKey, config.getSenderId(),
                    config.getBaseUrl(), config.getProviderMetadata());
        }
        return new GenericRestSmsProvider(apiKey, apiSecret, config.getSenderId(),
                config.getBaseUrl(), config.getProviderMetadata());
    }

    public static boolean isTwilio(String provider) {
        return provider != null && TWILIO.equalsIgnoreCase(provider.trim());
    }

    static boolean isAnbernet(String provider, String baseUrl) {
        if (provider != null && ANBERNET_PROVIDERS.contains(provider.trim().toUpperCase())) {
            return true;
        }
        return baseUrl != null && baseUrl.toLowerCase().contains("anbernet");
    }
}
