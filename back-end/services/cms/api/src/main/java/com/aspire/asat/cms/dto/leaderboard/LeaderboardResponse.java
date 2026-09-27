package com.aspire.asat.cms.dto.leaderboard;

import lombok.Data;

@Data
public class LeaderboardResponse {
    private String message;
    private int statusCode;
    private LeaderboardDTO[] data;
}
