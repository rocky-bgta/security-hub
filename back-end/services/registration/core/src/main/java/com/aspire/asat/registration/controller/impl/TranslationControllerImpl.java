package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.TranslationController;
import com.aspire.asat.registration.service.YamlMessageSource;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

@RestController
public class TranslationControllerImpl implements TranslationController {

    private YamlMessageSource yamlMessageSource;

    public TranslationControllerImpl(YamlMessageSource yamlMessageSource) {
        this.yamlMessageSource = yamlMessageSource;
    }

    @Override
    public Map<String, String> getAllMessages(@RequestParam(name = "lang", defaultValue = "en") String lang) {
      Locale locale = new Locale(lang);
      return  yamlMessageSource.getAllMessages(locale);
    }
}
