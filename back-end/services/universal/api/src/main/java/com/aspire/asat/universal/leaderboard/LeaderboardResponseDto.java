package com.aspire.asat.universal.leaderboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResponseDto {

    private UUID id;

    private String title; // Unique per client/admin

    private String name; // Can be duplicate

    private String designation;

    private VideoType videoType;

    private String videoUrl;

    private String thumbnailUrl;

    private LeaderboardStatus status; // ACTIVE or INACTIVE

    private Instant createdDate;

    private String createdBy;

    private String updatedBy;

    private Instant updatedAt;

    private UUID clientId;

    private String clientName; // For admin view

    private Boolean isDefault;
}
