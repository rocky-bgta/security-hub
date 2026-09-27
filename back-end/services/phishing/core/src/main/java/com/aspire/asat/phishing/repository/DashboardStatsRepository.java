package com.aspire.asat.phishing.repository;

import com.aspire.asat.phishing.dto.enums.CampaignChannel;
import com.aspire.asat.phishing.dto.enums.StatsPeriod;
import com.aspire.asat.phishing.model.DashboardStats;
import com.aspire.asat.phishing.repository.custom.DashboardStatsRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for dashboard statistics.
 */
@Repository
public interface DashboardStatsRepository extends MongoRepository<DashboardStats, String>, DashboardStatsRepositoryCustom {

    /**
     * Find stats by client, period, and date
     */
    Optional<DashboardStats> findByClientIdAndPeriodAndDate(
            String clientId, StatsPeriod period, LocalDate date);

    /**
     * Find stats by client, period, date, and channel.
     */
    Optional<DashboardStats> findByClientIdAndPeriodAndDateAndChannel(
            String clientId, StatsPeriod period, LocalDate date, CampaignChannel channel);

    /**
     * Find stats for date range
     */
    List<DashboardStats> findByClientIdAndPeriodAndDateBetweenOrderByDateAsc(
            String clientId, StatsPeriod period, LocalDate startDate, LocalDate endDate);

    /**
     * Find latest stats by period
     */
    Optional<DashboardStats> findTopByClientIdAndPeriodOrderByDateDesc(
            String clientId, StatsPeriod period);

    /**
     * Find stats for last N days (all channels). Prefer {@link #findDailyStatsForLastNDays(String, LocalDate, CampaignChannel)}.
     */
    @Query("{ 'clientId': ?0, 'period': 'DAILY', 'date': { $gte: ?1 } }")
    List<DashboardStats> findDailyStatsForLastNDays(String clientId, LocalDate startDate);

    /**
     * Delete old stats (cleanup)
     */
    void deleteByClientIdAndPeriodAndDateBefore(
            String clientId, StatsPeriod period, LocalDate beforeDate);
}
