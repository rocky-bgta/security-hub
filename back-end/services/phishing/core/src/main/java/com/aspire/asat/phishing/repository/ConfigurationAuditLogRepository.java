package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.ConfigurationAuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationAuditLogRepository extends MongoRepository<ConfigurationAuditLog, String> {
}
