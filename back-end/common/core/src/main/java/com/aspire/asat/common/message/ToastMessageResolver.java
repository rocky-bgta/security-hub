package com.aspire.asat.common.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a message key from the toast catalog, or returns the original text if no catalog entry exists.
 */
@Component
@RequiredArgsConstructor
public class ToastMessageResolver {

    private final MessageService messageService;

    public String resolve(String messageOrKey) {
        if (messageOrKey == null || messageOrKey.isBlank()) {
            return messageOrKey;
        }
        return messageService.get(messageOrKey);
    }
}
