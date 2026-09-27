package com.aspire.asat.universal.repository;

import java.util.List;
import java.util.UUID;

public interface PollSurveyVoteLogRepositoryCustom {
    List<Object[]> countVotesByPollSurveyId(UUID pollSurveyId);
}

