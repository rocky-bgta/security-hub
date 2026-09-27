package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.leaderboard.LeaderboardDTO;
import reactor.core.publisher.Flux;

import java.util.List;

public interface LeaderboardService {
    List<LeaderboardDTO> getLeaderboardData();
}
