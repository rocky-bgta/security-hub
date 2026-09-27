package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.model.AiGenerationJob;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiGenerationJobRepository extends MongoRepository<AiGenerationJob, String> {
}
