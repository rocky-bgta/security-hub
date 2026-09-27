package com.aspire.asat.notification.service.impl;

import com.aspire.asat.notification.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateEngine templateEngine;

    @Override
    public String generateHtml(String templateId, Map<String, Object> model) {
        Context context = new Context();
        if(model != null) {
            context.setVariables(model);
        }
        return templateEngine.process(templateId, context);
    }
}
