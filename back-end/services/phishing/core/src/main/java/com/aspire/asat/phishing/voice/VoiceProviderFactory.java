package com.aspire.asat.phishing.voice;

import com.aspire.asat.phishing.dto.enums.VoiceProviderType;
import com.aspire.asat.phishing.model.VoiceServerConfiguration;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import com.aspire.asat.phishing.voice.impl.GenericSipVoiceProvider;
import com.aspire.asat.phishing.voice.impl.TwilioVoiceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VoiceProviderFactory {

    private final CredentialEncryptionService credentialEncryptionService;

    public VoiceProvider create(VoiceServerConfiguration config) {
        String apiKey = credentialEncryptionService.decrypt(config.getApiKey());
        String apiSecret = credentialEncryptionService.decrypt(config.getApiSecret());
        if (config.getProvider() == VoiceProviderType.TWILIO) {
            return new TwilioVoiceProvider(apiKey, apiSecret, config.getCallerId(), config.getBaseUrl());
        }
        return new GenericSipVoiceProvider(apiKey, apiSecret, config.getCallerId(),
                config.getBaseUrl(), config.getProviderMetadata());
    }
}
