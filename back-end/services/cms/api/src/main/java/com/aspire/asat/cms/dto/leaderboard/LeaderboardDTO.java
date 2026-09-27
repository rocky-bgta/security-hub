package com.aspire.asat.cms.dto.leaderboard;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class LeaderboardDTO {
    private String leaderBoardId;
    private String name;
    private String designation;
    private String text;
    private String videoUrl;
    private Instant createdAt;
}
