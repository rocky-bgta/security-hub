package com.aspire.asat.phishing.voice;

import com.aspire.asat.phishing.dto.enums.VoiceProviderType;

public interface VoiceProvider {

    VoiceCallResult initiateCall(VoiceCallRequest request);

    VoiceProviderType getProviderType();
}
