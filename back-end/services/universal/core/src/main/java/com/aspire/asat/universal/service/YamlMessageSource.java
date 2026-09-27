package com.aspire.asat.universal.service;

import java.util.Locale;
import java.util.Map;

public interface YamlMessageSource {

    Map<String, String> getAllMessages(Locale locale);

}
