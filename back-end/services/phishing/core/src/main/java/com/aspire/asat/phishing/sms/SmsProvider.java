package com.aspire.asat.phishing.sms;

import com.aspire.asat.phishing.dto.enums.SmsProviderType;

public interface SmsProvider {

    SmsSendResult send(String mobileNumber, String message);

    SmsProviderType getProviderType();
}
