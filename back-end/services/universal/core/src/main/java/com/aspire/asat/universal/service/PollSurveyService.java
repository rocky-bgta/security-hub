package com.aspire.asat.universal.service;

import com.aspire.asat.universal.pool.PollSurveyCreateRequest;
import com.aspire.asat.universal.pool.PollSurveyResponse;
import com.aspire.asat.universal.pool.PollSurveySummaryResponse;
import com.aspire.asat.universal.pool.PollSurveyUpdateRequest;
import com.aspire.asat.universal.pool.PollSurveyVoteRequest;
import com.aspire.asat.universal.pool.PollSurveyDetailWithSummaryResponse;
import com.aspire.asat.universal.enums.PollSurveyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.aspire.asat.universal.universal.data.apiResponses.OffsetPageDto;

import java.util.UUID;

public interface PollSurveyService {
    PollSurveyResponse createPollSurvey(PollSurveyCreateRequest req);
    PollSurveyResponse updatePollSurvey(PollSurveyUpdateRequest req, UUID id);
    Page<PollSurveyResponse> getAllPolls(Pageable pageable);
    Page<PollSurveyResponse> getActivePollsOrSurveys(Pageable pageable);

    // New: offset/pageSize based paging with search and filters
    OffsetPageDto<PollSurveyResponse> getAllPollsPage(int offset, int pageSize, String search, String type, String status);
    OffsetPageDto<PollSurveyResponse> getActivePollsPage(int offset, int pageSize);

    void recordVote(UUID pollSurveyId, UUID questionId, UUID answerId);
    void recordVotes(UUID pollSurveyId, PollSurveyVoteRequest req);
    PollSurveyResponse setPollStatus(UUID pollSurveyId, PollSurveyStatus status);
    PollSurveySummaryResponse getPollSurveySummary(UUID pollSurveyId);
    PollSurveyDetailWithSummaryResponse getPollSurveyDetailWithSummary(UUID pollSurveyId);
    PollSurveyDetailWithSummaryResponse getLatestActivePollWithUserVotes();
}
