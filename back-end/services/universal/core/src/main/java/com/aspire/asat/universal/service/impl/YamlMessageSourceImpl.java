package com.aspire.asat.universal.service.impl;

import com.aspire.asat.universal.service.YamlMessageSource;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

public class YamlMessageSourceImpl extends AbstractMessageSource implements YamlMessageSource {

    private static final String BASE_NAME = "messages";
    private final Map<String, Properties> localePropertiesMap = new HashMap<>();

    public YamlMessageSourceImpl() {
        loadYamlMessages();
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        return Optional.ofNullable(localePropertiesMap.get(locale.getLanguage()))
                .map(props -> props.getProperty(code))
                .map(message -> new MessageFormat(message, locale))
                .orElse(null);
    }

    @Override
    public Map<String, String> getAllMessages(Locale locale) {
        Properties props = localePropertiesMap.get(locale.getLanguage());
        Map<String, String> messages = new HashMap<>();
        if (props != null) props.forEach((k, v) -> messages.put(k.toString(), v.toString()));
        return messages;
    }

    private void loadYamlMessages() {
        Yaml yaml = new Yaml();
        for (Locale locale : Locale.getAvailableLocales()) {
            String localeFile = BASE_NAME + "_" + locale.getLanguage() + ".yml";
            ClassPathResource resource = new ClassPathResource(localeFile);
            if (resource.exists()) {
                try (InputStream input = resource.getInputStream()) {
                    Properties props = new Properties();
                    flattenYaml(yaml.load(input), "", props);
                    localePropertiesMap.put(locale.getLanguage(), props);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void flattenYaml(Map<String, Object> source, String prefix, Properties props) {
        source.forEach((key, value) -> {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
            if (value instanceof Map) {
                flattenYaml((Map<String, Object>) value, fullKey, props);
            } else {
                props.put(fullKey, value.toString());
            }
        });
    }
}
