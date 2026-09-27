package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.PollSurveyAnswer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PollSurveyAnswerRepository extends MongoRepository<PollSurveyAnswer, UUID> {
}

