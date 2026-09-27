package com.aspire.asat.phishing.service.base;

import com.aspire.asat.common.enums.ResponseMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LocaleMessageService {
    private final HttpServletRequest request;
    private final MessageSource messageSource;

    public Locale getLocale() {
        return request.getLocale();
    }

    public String getLocalMessage(ResponseMessage key, Object... objects) {
        return messageSource.getMessage(key.getResponseMessage(), objects, getLocale());
    }

    public String getLocalMessage(ResponseMessage key) {
        return messageSource.getMessage(key.getResponseMessage(), null, getLocale());
    }

    public String getLocalMessage(String key, Object... objects) {
        return messageSource.getMessage(key, objects, getLocale());
    }

    public String getLocalMessage(String key) {
        return messageSource.getMessage(key, null, getLocale());
    }
}

