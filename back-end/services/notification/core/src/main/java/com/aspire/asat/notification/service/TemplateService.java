package com.aspire.asat.notification.service;

import java.util.Map;

public interface TemplateService {
    String generateHtml(String templateId, Map<String, Object> model);
}
