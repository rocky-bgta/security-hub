package com.aspire.asat.phishing.model;

import com.aspire.asat.phishing.dto.enums.ConfigurationAuditEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * Audit log for SMS server and campaign delivery configuration changes.
 */
@Document(collection = "configuration_audit_logs")
@CompoundIndex(name = "client_entity_idx", def = "{'clientId': 1, 'entityType': 1, 'timestamp': -1}")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationAuditLog {

    @Id
    private String id;

    private String clientId;

    private ConfigurationAuditEntityType entityType;

    private String entityId;

    private String action;

    private String actor;

    private Map<String, Object> beforeSnapshot;

    private Map<String, Object> afterSnapshot;

    @CreatedDate
    private Instant timestamp;
}
