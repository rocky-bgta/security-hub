package com.aspire.asat.cms.repository.topic;

import com.aspire.asat.cms.model.topic.Compliance;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplianceRepository extends MongoRepository<Compliance, String> {
    boolean existsByAcronymIgnoreCase(String acronym);
}
