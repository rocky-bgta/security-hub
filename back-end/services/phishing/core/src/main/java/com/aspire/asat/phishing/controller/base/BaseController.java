package com.aspire.asat.phishing.controller.base;

import com.aspire.asat.common.enums.ResponseMessage;
import com.aspire.asat.phishing.service.base.LocaleMessageService;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseController {

    protected LocaleMessageService localeMessageService;

    @Autowired
    public void setLocaleMessageService(LocaleMessageService localeMessageService) {
        this.localeMessageService = localeMessageService;
    }

    public String getMessage(ResponseMessage key) {
        return localeMessageService.getLocalMessage(key);
    }
}

