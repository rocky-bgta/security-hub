package com.aspire.asat.leaderboard;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardStatusUpdateDto {

    @NotNull(message = "Leaderboard ID is required")
    private UUID id;

    @NotNull(message = "Status is required")
    private LeaderboardStatus status; // ACTIVE or INACTIVE
}
