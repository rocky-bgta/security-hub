package com.aspire.asat.auth.service;

import com.aspire.asat.auth.components.MessageSourceComponent;
import com.aspire.asat.auth.dto.enums.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LocaleMessageService {
    private final MessageSourceComponent messageSource;
    private final HttpServletRequest httpServletRequest;

    public String getLocalMessage(String key) {
        try {
            return messageSource.getMessage(key, null, getLocale());
        } catch (Exception ignored) {
            return key;
        }
    }

    public String getLocalMessage(ResponseMessage key) {
        return this.getLocalMessage(key.getResponseMessage());
    }

    private Locale getLocale() {
        return httpServletRequest.getLocale();
    }
}
