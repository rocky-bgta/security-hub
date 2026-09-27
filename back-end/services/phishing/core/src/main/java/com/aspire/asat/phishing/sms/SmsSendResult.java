package com.aspire.asat.phishing.sms;

public record SmsSendResult(boolean success, String messageId, String errorCode, String errorMessage) {

    public static SmsSendResult ok(String messageId) {
        return new SmsSendResult(true, messageId, null, null);
    }

    public static SmsSendResult fail(String errorCode, String errorMessage) {
        return new SmsSendResult(false, null, errorCode, errorMessage);
    }
}
