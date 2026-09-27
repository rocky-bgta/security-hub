package com.aspire.asat.universal.repository.leaderboard;

import com.aspire.asat.universal.leaderboard.LeaderboardStatus;
import com.aspire.asat.universal.leaderboard.VideoType;
import com.aspire.asat.universal.entity.leaderboard.Leaderboard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeaderboardRepository extends MongoRepository<Leaderboard, UUID>, LeaderboardRepositoryCustom {

    // Find all leaderboards by clientId
    List<Leaderboard> findByClientId(UUID clientId);

    // Find all default (admin-created) leaderboards
    List<Leaderboard> findByIsDefaultTrue();

    // Find leaderboards by clientId or default
    List<Leaderboard> findByClientIdOrIsDefaultTrue(UUID clientId, Boolean isDefault);

    // Check if title exists (case insensitive) for a specific client
    boolean existsByTitleIgnoreCaseAndClientId(String title, UUID clientId);

    // Check if title exists (case insensitive) for default leaderboards
    boolean existsByTitleIgnoreCaseAndIsDefaultTrue(String title);

    // Count by status
    long countByStatus(LeaderboardStatus status);

    // Count by clientId and status
    long countByClientIdAndStatus(UUID clientId, LeaderboardStatus status);

    // Count by videoType
    long countByVideoType(VideoType videoType);

    // Count by clientId and videoType
    long countByClientIdAndVideoType(UUID clientId, VideoType videoType);

    // Count by clientId
    long countByClientId(UUID clientId);

    // Count default leaderboards
    long countByIsDefaultTrue();

    // Find active leaderboards by clientId
    List<Leaderboard> findByClientIdAndStatusOrderByCreatedDateDesc(UUID clientId, LeaderboardStatus status);

    // Find active default leaderboards
    List<Leaderboard> findByIsDefaultTrueAndStatusOrderByCreatedDateDesc(LeaderboardStatus status);

    long countByIsDefaultTrueAndStatus(LeaderboardStatus leaderboardStatus);
}
