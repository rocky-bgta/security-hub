package com.aspire.asat.universal.poll.service;

import com.aspire.asat.universal.pool.PollSurveyCreateRequest;
import com.aspire.asat.universal.pool.PollSurveyResponse;
import com.aspire.asat.universal.pool.PollSurveySummaryResponse;
import com.aspire.asat.universal.pool.PollSurveyUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PollSurveyService {
    PollSurveyResponse createPollSurvey(PollSurveyCreateRequest req);
    PollSurveyResponse updatePollSurvey(PollSurveyUpdateRequest req, UUID id);
    Page<PollSurveyResponse> getAllPolls(Pageable pageable);
    Page<PollSurveyResponse> getActivePollsOrSurveys(Pageable pageable);
    void recordVote(UUID pollSurveyId, UUID questionId, UUID answerId);
    PollSurveySummaryResponse getPollSurveySummary(UUID pollSurveyId);
}
