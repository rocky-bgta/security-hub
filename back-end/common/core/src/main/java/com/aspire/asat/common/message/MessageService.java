package com.aspire.asat.common.message;

import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Resolves toast messages from the centralized YAML catalog.
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final ToastMessageSource toastMessageSource;

    public String get(String key) {
        return toastMessageSource.getMessage(key, null, resolveLocale());
    }

    public String get(String key, Object... args) {
        return toastMessageSource.getMessage(key, args, resolveLocale());
    }

    private Locale resolveLocale() {
        Locale locale = LocaleContextHolder.getLocale();
        return locale != null ? locale : Locale.ENGLISH;
    }
}
