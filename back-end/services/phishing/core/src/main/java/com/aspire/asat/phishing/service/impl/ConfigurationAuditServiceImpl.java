package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.enums.ConfigurationAuditEntityType;
import com.aspire.asat.phishing.model.ConfigurationAuditLog;
import com.aspire.asat.phishing.repository.ConfigurationAuditLogRepository;
import com.aspire.asat.phishing.service.ConfigurationAuditService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigurationAuditServiceImpl implements ConfigurationAuditService {

    private final ConfigurationAuditLogRepository auditLogRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public void logChange(String clientId, ConfigurationAuditEntityType entityType, String entityId,
                          String action, Map<String, Object> beforeSnapshot, Map<String, Object> afterSnapshot) {
        String actor = userCurrentContextService.getCurrentUserContext().getUserId();
        ConfigurationAuditLog entry = ConfigurationAuditLog.builder()
                .clientId(clientId)
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .actor(actor)
                .beforeSnapshot(beforeSnapshot)
                .afterSnapshot(afterSnapshot)
                .build();
        auditLogRepository.save(entry);
        log.info("Configuration audit: clientId={} entityType={} entityId={} action={} actor={}",
                clientId, entityType, entityId, action, actor);
    }
}
