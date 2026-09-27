package com.aspire.asat.auth.constant;

import com.aspire.asat.auth.dto.enums.ResponseMessage;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OtpMessagesPropertiesTest {

    @Test
    void messageEnProperties_ContainExactSpecCopy() throws Exception {
        Properties properties = loadFromServiceModule();

        assertEquals("Invalid verification code. Please try again.",
                properties.getProperty("mfa.otp.invalid"));
        assertEquals("This verification code has expired. Please request a new code.",
                properties.getProperty("mfa.otp.expired"));
        assertEquals("Too many incorrect attempts. Verification has been temporarily locked.",
                properties.getProperty("mfa.otp.locked"));
        assertEquals("Too many verification code requests. Please try again later.",
                properties.getProperty("mfa.otp.rate.limited"));
        assertEquals("A new verification code has been sent to your registered email address.",
                properties.getProperty("mfa.otp.resend.email.successful"));
        assertEquals("A new verification code has been sent.",
                properties.getProperty("mfa.otp.resend.sms.successful"));
        assertEquals("A new verification code has been sent.",
                properties.getProperty("mfa.otp.resend.phone.call.successful"));
    }

    @Test
    void responseMessageEnum_MatchesOtpKeys() {
        assertEquals(OtpMessages.INVALID, ResponseMessage.MFA_OTP_INVALID.getResponseMessage());
        assertEquals(OtpMessages.EXPIRED, ResponseMessage.MFA_OTP_EXPIRED.getResponseMessage());
        assertEquals(OtpMessages.LOCKED, ResponseMessage.MFA_OTP_LOCKED.getResponseMessage());
        assertEquals(OtpMessages.RATE_LIMITED, ResponseMessage.MFA_OTP_RATE_LIMITED.getResponseMessage());
    }

    private static Properties loadFromServiceModule() throws Exception {
        java.nio.file.Path path = java.nio.file.Path.of(
                "../service/src/main/resources/i18n/message_en.properties");
        if (!java.nio.file.Files.exists(path)) {
            path = java.nio.file.Path.of(
                    "services/auth/service/src/main/resources/i18n/message_en.properties");
        }
        Properties properties = new Properties();
        try (InputStream in = java.nio.file.Files.newInputStream(path)) {
            properties.load(in);
        }
        return properties;
    }
}
