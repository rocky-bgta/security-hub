package com.aspire.asat.universal.entity.leaderboard;

import com.aspire.asat.universal.leaderboard.LeaderboardStatus;
import com.aspire.asat.universal.leaderboard.VideoType;
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

    private VideoType videoType;

    private String videoUrl;

    private String thumbnailUrl;

    private LeaderboardStatus status; // ACTIVE or INACTIVE

    private Instant createdDate;

    private String createdBy;

    private String updatedBy;

    private Instant updatedAt;

    private UUID clientId; // null for admin-created default leaderboards

    private Boolean isDefault; // true for admin-created default leaderboards
}
