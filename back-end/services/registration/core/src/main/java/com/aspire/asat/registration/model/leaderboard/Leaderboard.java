package com.aspire.asat.registration.model.leaderboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "leaderboards")
public class Leaderboard {

    @Id
    private UUID id;

    private String title; // Unique per client/admin

    private String name; // Can be duplicate

    private String designation;

    private com.aspire.asat.leaderboard.VideoType videoType;

    private String videoUrl;

    private String thumbnailUrl;

    private com.aspire.asat.leaderboard.LeaderboardStatus status; // ACTIVE or INACTIVE

    private Instant createdDate;

    private String createdBy;

    private String updatedBy;

    private Instant updatedAt;

    private UUID clientId; // null for admin-created default leaderboards

    private Boolean isDefault; // true for admin-created default leaderboards
}
