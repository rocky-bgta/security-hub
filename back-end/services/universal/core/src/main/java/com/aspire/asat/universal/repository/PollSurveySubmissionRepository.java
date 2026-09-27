package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.PollSurveySubmission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PollSurveySubmissionRepository extends MongoRepository<PollSurveySubmission, UUID> {
    boolean existsByUserIdAndPollSurveyId(String userId, UUID pollSurveyId);
}

