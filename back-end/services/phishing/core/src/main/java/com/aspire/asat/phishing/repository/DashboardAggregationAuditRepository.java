package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.DashboardAggregationAudit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DashboardAggregationAuditRepository extends MongoRepository<DashboardAggregationAudit, String> {
    Optional<DashboardAggregationAudit> findTopByClientIdOrderByRunAtDesc(String clientId);
}
