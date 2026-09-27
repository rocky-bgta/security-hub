package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.PollSurveyQuestion;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PollSurveyQuestionRepository extends MongoRepository<PollSurveyQuestion, UUID> {
}
