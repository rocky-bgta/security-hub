package com.aspire.asat.phishing.voice.impl;

import com.aspire.asat.common.util.PhoneNumberUtils;
import com.aspire.asat.phishing.dto.enums.VoiceProviderType;
import com.aspire.asat.phishing.voice.VoiceCallRequest;
import com.aspire.asat.phishing.voice.VoiceCallResult;
import com.aspire.asat.phishing.voice.VoiceProvider;
import com.twilio.http.HttpMethod;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.CallCreator;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;

@Slf4j
public class TwilioVoiceProvider implements VoiceProvider {

    private static final List<String> STATUS_CALLBACK_EVENTS =
            List.of("initiated", "ringing", "answered", "completed");

    private final String fromNumber;
    private final String twimlBaseUrl;
    private final TwilioRestClient restClient;

    public TwilioVoiceProvider(String accountSid, String authToken, String fromNumber, String twimlBaseUrl) {
        this.fromNumber = fromNumber;
        this.twimlBaseUrl = twimlBaseUrl;
        // Per-call client avoids the global Twilio.init() race across concurrent
        // campaigns/tenants that use different Twilio accounts.
        this.restClient = new TwilioRestClient.Builder(accountSid, authToken).build();
    }

    @Override
    public VoiceCallResult initiateCall(VoiceCallRequest request) {
        try {
            String to = PhoneNumberUtils.normalize(request.toPhone());
            String from = PhoneNumberUtils.normalize(fromNumber != null ? fromNumber : request.callerId());

            CallCreator creator = Call.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(from),
                    URI.create(resolveTwimlUrl(request)));

            if (StringUtils.hasText(request.statusCallbackUrl())) {
                creator.setStatusCallback(URI.create(request.statusCallbackUrl()))
                        .setStatusCallbackMethod(HttpMethod.POST)
                        .setStatusCallbackEvent(STATUS_CALLBACK_EVENTS);
            }

            Call call = creator.create(restClient);
            return VoiceCallResult.ok(call.getSid());
        } catch (Exception e) {
            log.error("Twilio voice call failed: {}", e.getMessage(), e);
            return VoiceCallResult.fail(e.getMessage());
        }
    }

    private String resolveTwimlUrl(VoiceCallRequest request) {
        if (StringUtils.hasText(request.twimlUrl())) {
            return request.twimlUrl();
        }
        if (StringUtils.hasText(twimlBaseUrl)) {
            return twimlBaseUrl;
        }
        throw new IllegalStateException("No TwiML URL available for Twilio call");
    }

    @Override
    public VoiceProviderType getProviderType() {
        return VoiceProviderType.TWILIO;
    }
}
