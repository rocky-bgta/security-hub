package com.aspire.asat.universal.leaderboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardSummaryDto {

    private long totalLeaders;

    private long activeLeaders;

    private long inactiveLeaders;

    private long uploadedVideos;
}

