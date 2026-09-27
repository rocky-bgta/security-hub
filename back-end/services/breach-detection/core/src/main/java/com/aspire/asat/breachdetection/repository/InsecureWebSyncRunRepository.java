package com.aspire.asat.breachdetection.repository;

import com.aspire.asat.breachdetection.model.InsecureWebSyncRun;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsecureWebSyncRunRepository extends MongoRepository<InsecureWebSyncRun, String> {
}
