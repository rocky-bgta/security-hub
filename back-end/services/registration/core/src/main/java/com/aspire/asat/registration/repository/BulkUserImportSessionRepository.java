package com.aspire.asat.registration.repository;

import com.aspire.asat.registration.model.BulkUserImportSession;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BulkUserImportSessionRepository extends MongoRepository<BulkUserImportSession, String> {
}
