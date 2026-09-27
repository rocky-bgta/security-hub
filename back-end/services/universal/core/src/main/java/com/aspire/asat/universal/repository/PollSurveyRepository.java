package com.aspire.asat.universal.repository;

import com.aspire.asat.universal.entity.PollSurvey;
import com.aspire.asat.universal.enums.PollSurveyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PollSurveyRepository extends MongoRepository<PollSurvey, UUID>, PollSurveyRepositoryCustom {
    Page<PollSurvey> findByStatus(PollSurveyStatus status, Pageable pageable);
    long countByStatus(PollSurveyStatus status);
    List<PollSurvey> findByStatusOrderByCreatedAtDesc(PollSurveyStatus status, Pageable pageable);
}
