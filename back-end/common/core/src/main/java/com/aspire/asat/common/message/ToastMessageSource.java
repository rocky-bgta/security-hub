package com.aspire.asat.common.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Loads toast messages from {@code messages/toast-messages_{lang}.yml} on the classpath.
 */
@Component("toastMessageSource")
public class ToastMessageSource extends AbstractMessageSource {

    private static final Logger log = LoggerFactory.getLogger(ToastMessageSource.class);
    private static final String FILE_PATH_FORMAT = "messages/toast-messages_%s.yml";
    private static final String DEFAULT_LANGUAGE = "en";

    private final Map<String, Properties> localePropertiesMap = new HashMap<>();

    public ToastMessageSource() {
        loadYamlMessages();
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String language = locale != null ? locale.getLanguage() : DEFAULT_LANGUAGE;
        String message = resolveMessage(code, language);
        if (message == null && !DEFAULT_LANGUAGE.equals(language)) {
            message = resolveMessage(code, DEFAULT_LANGUAGE);
        }
        if (message == null) {
            log.warn("Toast message key not found: {}", code);
            return new MessageFormat(code, locale != null ? locale : Locale.ENGLISH);
        }
        return new MessageFormat(message, locale != null ? locale : Locale.ENGLISH);
    }

    private String resolveMessage(String code, String language) {
        return Optional.ofNullable(localePropertiesMap.get(language))
                .map(props -> props.getProperty(code))
                .orElse(null);
    }

    private void loadYamlMessages() {
        Yaml yaml = new Yaml();
        loadLocaleFile(yaml, DEFAULT_LANGUAGE);
        for (Locale locale : Locale.getAvailableLocales()) {
            String lang = locale.getLanguage();
            if (!DEFAULT_LANGUAGE.equals(lang) && !localePropertiesMap.containsKey(lang)) {
                loadLocaleFile(yaml, lang);
            }
        }
    }

    private void loadLocaleFile(Yaml yaml, String language) {
        String filePath = String.format(FILE_PATH_FORMAT, language);
        ClassPathResource resource = new ClassPathResource(filePath);
        if (!resource.exists()) {
            return;
        }
        try (InputStream input = resource.getInputStream()) {
            Object loaded = yaml.load(input);
            if (loaded instanceof Map<?, ?> map) {
                Properties props = new Properties();
                flattenYaml(map, "", props);
                localePropertiesMap.put(language, props);
                log.info("Loaded {} toast messages for locale '{}'", props.size(), language);
            }
        } catch (Exception e) {
            log.error("Failed to load toast messages from {}", filePath, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void flattenYaml(Map<?, ?> source, String prefix, Properties props) {
        source.forEach((key, value) -> {
            String fullKey = prefix.isEmpty() ? key.toString() : prefix + "." + key;
            if (value instanceof Map<?, ?> nested) {
                flattenYaml(nested, fullKey, props);
            } else if (value != null) {
                props.put(fullKey, value.toString());
            }
        });
    }
}
