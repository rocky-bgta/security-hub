package com.aspire.asat.phishing.sms.impl;

import com.aspire.asat.common.util.PhoneNumberUtils;
import com.aspire.asat.phishing.dto.enums.SmsProviderType;
import com.aspire.asat.phishing.sms.SmsProvider;
import com.aspire.asat.phishing.sms.SmsSendResult;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TwilioSmsProvider implements SmsProvider {

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;

    public TwilioSmsProvider(String accountSid, String authToken, String fromNumber) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
        Twilio.init(accountSid, authToken);
    }

    @Override
    public SmsSendResult send(String mobileNumber, String message) {
        try {
            String to = PhoneNumberUtils.normalize(mobileNumber);
            String from = PhoneNumberUtils.normalize(fromNumber);
            Message twilioMessage = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(from),
                    message
            ).create();
            return SmsSendResult.ok(twilioMessage.getSid());
        } catch (Exception e) {
            log.error("Twilio SMS send failed: {}", e.getMessage(), e);
            return SmsSendResult.fail("TWILIO_ERROR", e.getMessage());
        }
    }

    @Override
    public SmsProviderType getProviderType() {
        return SmsProviderType.TWILIO;
    }
}
