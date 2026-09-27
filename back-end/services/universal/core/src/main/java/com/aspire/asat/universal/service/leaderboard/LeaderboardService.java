package com.aspire.asat.universal.service.leaderboard;

import com.aspire.asat.universal.leaderboard.LeaderboardRequestDto;
import com.aspire.asat.universal.leaderboard.LeaderboardResponseDto;
import com.aspire.asat.universal.leaderboard.LeaderboardStatusUpdateDto;
import com.aspire.asat.universal.leaderboard.LeaderboardSummaryDto;
import com.aspire.asat.universal.universal.data.apiResponses.AllResponseDto;

import java.util.List;
import java.util.UUID;

public interface LeaderboardService {

    /**
     * Create a new leaderboard
     * - Admin can create for anyone or as default
     * - Client can create for themselves
     */
    LeaderboardResponseDto createLeaderboard(LeaderboardRequestDto request);

    /**
     * Get all leaderboards based on user role (For admin/client/msp panel)
     * - Admin: sees all leaderboards (all clients + admin's own)
     * - Client: sees their own + admin's default leaderboards
     * - MSP: sees active Aspire defaults + active leaderboards for all client admins in CurrentContext
     * Supports search by title, name, designation
     * Ordered by created date DESC
     * @param offset page offset (0-based)
     * @param pageSize number of items per page
     * @param search search term (searches title, name, designation)
     * @return paginated leaderboard results
     */
    AllResponseDto<List<LeaderboardResponseDto>> getAllLeaderboards(int offset, int pageSize, String search);

    /**
     * Get exactly 2 active leaderboards for web/frontend
     * Priority: Client's active leaderboards first, then admin defaults
     * Always returns up to 2 active leaderboards
     */
    List<LeaderboardResponseDto> getActiveLeaderboardsForWeb();

    /**
     * Get leaderboard by ID
     */
    LeaderboardResponseDto getLeaderboardById(UUID id);

    /**
     * Update leaderboard
     * - Admin can update anyone's leaderboard
     * - Client can only update their own leaderboard
     */
    LeaderboardResponseDto updateLeaderboard(UUID id, LeaderboardRequestDto request);

    /**
     * Delete leaderboard
     * - Admin can delete anyone's leaderboard
     * - Client can only delete their own leaderboard
     */
    void deleteLeaderboard(UUID id);

    /**
     * Update leaderboard status
     * - Admin can update anyone's status
     * - Client can only update their own status
     */
    LeaderboardResponseDto updateLeaderboardStatus(LeaderboardStatusUpdateDto request);

    /**
     * Get leaderboard summary
     */
    LeaderboardSummaryDto getLeaderboardSummary();
}
