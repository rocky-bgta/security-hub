package com.aspire.asat.registration.service;

import java.util.Locale;
import java.util.Map;

public interface YamlMessageSource {

    Map<String, String> getAllMessages(Locale locale);

}
