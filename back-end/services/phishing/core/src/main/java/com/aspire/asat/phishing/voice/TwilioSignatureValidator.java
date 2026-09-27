package com.aspire.asat.phishing.voice;

import com.twilio.security.RequestValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Validates Twilio's {@code X-Twilio-Signature} header so only authentic Twilio
 * callbacks are processed. The signature is computed by Twilio over the exact
 * public URL we configured plus the POST parameters, using the account's auth
 * token.
 */
@Slf4j
@Component
public class TwilioSignatureValidator {

    /**
     * @param authToken the Twilio account auth token for the calling account
     * @param url       the public URL Twilio requested (the one we configured)
     * @param params    the POST form parameters Twilio sent
     * @param signature the {@code X-Twilio-Signature} header value
     * @return {@code true} when the signature is valid
     */
    public boolean isValid(String authToken, String url, Map<String, String> params, String signature) {
        if (!StringUtils.hasText(authToken)) {
            log.warn("Twilio signature validation skipped: missing auth token");
            return false;
        }
        if (!StringUtils.hasText(signature)) {
            log.warn("Twilio signature validation failed: missing X-Twilio-Signature header");
            return false;
        }
        try {
            RequestValidator validator = new RequestValidator(authToken);
            return validator.validate(url, params, signature);
        } catch (Exception e) {
            log.error("Twilio signature validation error for url {}: {}", url, e.getMessage());
            return false;
        }
    }
}
