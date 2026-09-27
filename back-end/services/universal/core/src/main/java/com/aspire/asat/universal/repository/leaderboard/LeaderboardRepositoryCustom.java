package com.aspire.asat.universal.repository.leaderboard;

import com.aspire.asat.universal.entity.leaderboard.Leaderboard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface LeaderboardRepositoryCustom {

    /**
     * Find all leaderboards with search filter (searches title, name, designation)
     * Ordered by createdDate DESC
     * @param search search term (case-insensitive)
     * @return list of leaderboards ordered by createdDate DESC
     */
    List<Leaderboard> findAllWithSearch(String search);

    /**
     * Find all leaderboards with search filter and pagination
     * Ordered by createdDate DESC
     * @param search search term (case-insensitive)
     * @param pageable pagination info
     * @return page of leaderboards ordered by createdDate DESC
     */
    Page<Leaderboard> findAllWithSearchPaginated(String search, Pageable pageable);

    /**
     * Find leaderboards by clientId with search filter
     * Ordered by createdDate DESC
     * @param clientId client ID
     * @param search search term (case-insensitive)
     * @return list of leaderboards ordered by createdDate DESC
     */
    List<Leaderboard> findByClientIdWithSearch(UUID clientId, String search);

    /**
     * Find default leaderboards with search filter
     * Ordered by createdDate DESC
     * @param search search term (case-insensitive)
     * @return list of leaderboards ordered by createdDate DESC
     */
    List<Leaderboard> findDefaultWithSearch(String search);

    /**
     * Find leaderboards by clientId OR default with search filter
     * Ordered by createdDate DESC
     * @param clientId client ID
     * @param search search term (case-insensitive)
     * @return list of leaderboards ordered by createdDate DESC
     */
    List<Leaderboard> findByClientIdOrDefaultWithSearch(UUID clientId, String search);

    /**
     * Find leaderboards by clientId OR default with search filter and pagination
     * Ordered by createdDate DESC
     * @param clientId client ID
     * @param search search term (case-insensitive)
     * @param pageable pagination info
     * @return page of leaderboards ordered by createdDate DESC
     */
    Page<Leaderboard> findByClientIdOrDefaultWithSearchPaginated(UUID clientId, String search, Pageable pageable);

    /**
     * Find active leaderboards for multiple client IDs or active Aspire default leaderboards.
     * Ordered by createdDate DESC.
     */
    Page<Leaderboard> findActiveByClientIdsOrDefaultWithSearchPaginated(List<UUID> clientIds, String search, Pageable pageable);
}

