package com.aspire.asat.auth.components;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Properties;

@Component("messageSource")
public class MessageSourceComponent extends AbstractMessageSource {
    private final Logger logger = LoggerFactory.getLogger(MessageSourceComponent.class);
    private static final String FILE_NAME_FORMAT = "i18n/message_%s.properties";

    @Override
    protected MessageFormat resolveCode(String key, Locale locale) {
        String responseMessage = getResponseMessage(key, locale.getLanguage());
        return new MessageFormat(responseMessage, locale);
    }

    private String getResponseMessage(String key, String language) {
        String fileName = resolveMessageFileName(language);
        Properties properties = loadProperties(fileName);

        String message = properties.getProperty(key);
        if (StringUtils.isBlank(message)) {
            logger.error("Could not find message key: " + key + " in file: " + fileName);
            message = humanReadableText(key);
        }
        return message;
    }

    private Properties loadProperties(String fileName) {
        Properties properties = new Properties();
        try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (input != null) {
                properties.load(input);
            } else {
                logger.error("Could not find message file: " + fileName);
            }
        } catch (IOException ex) {
            logger.error("Error loading message file: " + fileName, ex);
        }
        return properties;
    }

    private String resolveMessageFileName(String language) {
        return String.format(FILE_NAME_FORMAT, language);
    }

    private String humanReadableText(String key) {
        try {
            String[] parts = key.split("\\.");
            StringBuilder formattedError = new StringBuilder();
            for (String part : parts) {
                if (!formattedError.isEmpty()) {
                    formattedError.append(' ');
                }
                formattedError.append(Character.toUpperCase(part.charAt(0)));
                formattedError.append(part.substring(1));
            }
            return formattedError.toString();
        } catch (Exception ex) {
            return key;
        }
    }
}
