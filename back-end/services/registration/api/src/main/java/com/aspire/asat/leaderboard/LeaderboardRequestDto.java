package com.aspire.asat.leaderboard;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardRequestDto {

    @NotBlank(message = "Title is required")
    private String title; // Unique per client/admin

    @NotBlank(message = "Name is required")
    private String name; // Can be duplicate

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotNull(message = "Video type is required")
    private VideoType videoType;
    private String videoUrl;
    private String thumbnailUrl;
    private LeaderboardStatus status; // ACTIVE or INACTIVE

}
