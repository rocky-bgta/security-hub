package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.PollSurveyVoteLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PollSurveyVoteLogRepository extends MongoRepository<PollSurveyVoteLog, UUID>, PollSurveyVoteLogRepositoryCustom {

    List<PollSurveyVoteLog> findByUserIdAndQuestionId(String userId, UUID questionId);

    List<PollSurveyVoteLog> findByUserIdAndPollSurveyId(String userId, UUID pollSurveyId);

    boolean existsByPollSurveyId(UUID pollSurveyId);
}
