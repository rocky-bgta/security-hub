package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.ConfigurationAuditEntityType;

import java.util.Map;

public interface ConfigurationAuditService {

    void logChange(String clientId, ConfigurationAuditEntityType entityType, String entityId,
                   String action, Map<String, Object> beforeSnapshot, Map<String, Object> afterSnapshot);
}
