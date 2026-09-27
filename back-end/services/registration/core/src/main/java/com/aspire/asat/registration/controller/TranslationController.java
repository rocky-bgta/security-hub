package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RequestMapping(value = WebApiUrlConstants.TRANSLATION_API, produces = "application/json")
public interface TranslationController {

    @GetMapping
    Map<String, String> getAllMessages(@RequestParam(name = "lang", defaultValue = "en") String lang);

}
